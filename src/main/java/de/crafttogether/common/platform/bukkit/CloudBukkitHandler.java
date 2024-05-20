package de.crafttogether.common.platform.bukkit;

import de.crafttogether.CTCommons;
import de.crafttogether.common.commands.CloudSimpleHandler;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.commands.ThrowingBiConsumer;
import de.crafttogether.common.localization.LocalizationEnum;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.common.util.CommonUtil;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.PreprocessorMapper;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.BukkitCommandManager;
import org.incendo.cloud.bukkit.BukkitCommandManager.BrigadierInitializationException;
import org.incendo.cloud.caption.Caption;
import org.incendo.cloud.caption.CaptionProvider;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.component.preprocessor.ComponentPreprocessor;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InjectionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.meta.CommandMeta;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ParserParameter;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.services.PipelineException;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Configures the Cloud Command Framework for basic use inside a Bukkit Paper or
 * Spigot server environment. Initializes the command manager itself as synchronously
 * executing (on main thread), registers annotations and help system, and also
 * registers some useful preprocessing logic.
 *
 * From annotation-registered commands you can obtain your plugin instance easily,
 * as it is made available through an injector by default.
 */
public class CloudBukkitHandler implements CloudSimpleHandler {
    private BukkitCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;
    private final Set<Class<?>> exceptionTypes = new HashSet<>();

    /**
     * Whether this handler for the Cloud Command Framework has been enabled
     *
     * @return True if enabled
     */
    @Override
    public boolean isEnabled() {
        return this.manager != null;
    }

    /**
     * Enables and initializes the Cloud Command Framework. After this is
     * called, commands and other things can be registered.
     *
     * @param plugin Owning Bukkit Plugin for this handler
     */
    @SuppressWarnings("unchecked")
    public void enable(Plugin plugin) {
        try {
            this.manager = new PaperCommandManager<>(
                    /* Owning plugin */ plugin,
                    /* Coordinator function */ ExecutionCoordinator.simpleCoordinator(),
                    /* Command Sender <-> C */ SenderMapper.create(this::getCommandSender, (sender) -> ((BukkitCommandSender) sender).getSender())
            );
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to initialize the command manager", e);
        }

        try {
            manager.registerBrigadier();
            CloudBrigadierManager<?, ?> brig = manager.brigadierManager();

            brig.setNativeNumberSuggestions(false);
        } catch (BrigadierInitializationException ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to register commands using brigadier, " +
                    "using fallback instead. Error:", ex);
        }

        // Create the annotation parser. This allows you to define commands using methods annotated with @Command
        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.manager,
                /* Command sender type */ CommandSender.class
        );

        // Fixes incorrect exception handling in Cloud, so that user-specified
        // exception types can be used instead.
        handle(CommandExecutionException.class, this::handleException);
        handle(PipelineException.class, this::handleException);
        handle(InjectionException.class, this::handleException);
    }

    public void enable() {
        if (isEnabled())
            return;

        enable(CTCommonsBukkit.plugin);
    }

    public CommandSender getCommandSender(org.bukkit.command.CommandSender bukkitCommandSender) {
        return new BukkitCommandSender(bukkitCommandSender);
    }

    @Override
    public void handleException(CommandSender sender, Throwable exception) throws Throwable {
        Throwable cause = exception.getCause();
        if (exceptionTypes.contains(cause.getClass())) {
            throw cause;
        } else {
            // Rethrow to pass to the next handler
            throw exception;
        }
    }

    /**
     * Gets the parser instance used to parse annotated commands
     *
     * @return parser
     */
    @Override
    public AnnotationParser<CommandSender> getParser() {
        return this.annotationParser;
    }

    /**
     * Gets the command manager
     *
     * @return manager
     */
    @Override
    public CommandManager<CommandSender> getManager() {
        return this.manager;
    }

    /**
     * Register a new command postprocessor. The order they are registered in is respected, and they
     * are called in FIFO order. This is called before the command handler is executed.
     *
     * @param processor Processor to register
     */
    @Override
    public void postProcess(final CommandPostprocessor<CommandSender> processor) {
        this.manager.registerCommandPostProcessor(processor);
    }

    /**
     * Register a parser
     *
     * @param descriptor The descriptor of the parser
     * @param <T>        Generic type specifying what is produced by the parser
     */
    @Override
    public <T> void parse(ParserDescriptor<CommandSender, T> descriptor) {
        this.manager.parserRegistry().registerParser(descriptor);
    }

    /**
     * Register a parser supplier
     *
     * @param type     The type that is parsed by the parser
     * @param supplier The function that generates the parser. The map supplied may contain parameters used
     *                 to configure the parser, many of which are documented in {@link org.incendo.cloud.parser.StandardParameters}
     * @param <T>      Generic type specifying what is produced by the parser
     */
    @Override
    public <T> void parse(
            Class<T> type,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    ) {
        this.manager.parserRegistry().registerParserSupplier(TypeToken.get(type), supplier);
    }

    /**
     * Register a parser supplier
     *
     * @param type     The type that is parsed by the parser
     * @param supplier The function that generates the parser. The map supplied my contain parameters used
     *                 to configure the parser, many of which are documented in {@link org.incendo.cloud.parser.StandardParameters}
     * @param <T>      Generic type specifying what is produced by the parser
     */
    @Override
    public <T> void parse(
            TypeToken<T> type,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    ) {
        this.manager.parserRegistry().registerParserSupplier(type, supplier);
    }

    /**
     * Register a named parser supplier
     *
     * @param name     Parser name
     * @param supplier The function that generates the parser. The map supplied my contain parameters used
     *                 to configure the parser, many of which are documented in {@link org.incendo.cloud.parser.StandardParameters}
     */
    @Override
    public void parse(
            String name,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    ) {
        this.manager.parserRegistry().registerNamedParserSupplier(name, supplier);
    }

    /**
     * Register a new named suggestion provider with a constant list of suggestions.
     *
     * @param name Name of the suggestions provider. The name is case independent.
     * @param suggestions List of suggestions
     */
    @Override
    public void suggest(String name, List<String> suggestions) {
        suggest(name, (sender, arg) -> suggestions);
    }

    /**
     * Register a new named suggestion provider with a no-input supplier for a list of suggestions.
     *
     * @param name Name of the suggestions provider. The name is case independent.
     * @param suggestionsProvider The suggestions provider
     */
    @Override
    public void suggest(String name, Supplier<List<String>> suggestionsProvider) {
        suggest(name, (sender, arg) -> suggestionsProvider.get());
    }

    /**
     * Register a new named suggestion provider.
     * When an argument suggestion is configured with this name, calls the function
     * to produce suggestions for that argument.
     *
     * @param name Name of the suggestions provider. The name is case independent.
     * @param suggestionsProvider The suggestions provider
     */
    @Override
    public void suggest(
            String name,
            BlockingSuggestionProvider.Strings<CommandSender> suggestionsProvider
    ) {
        manager.parserRegistry().registerSuggestionProvider(name, suggestionsProvider);
    }

    /**
     * Register an injector for a particular type.
     * Will automatically inject this type of object when provided in method signatures.
     *
     * @param clazz    Type that the injector should inject for. This type will matched using
     *                 {@link Class#isAssignableFrom(Class)}
     * @param value    The value to inject where clazz is used
     * @param <T>      Injected type
     */
    @Override
    public <T> void inject(
            final Class<T> clazz,
            final T value
    ) {
        injector(clazz, (context, annotations) -> value);
    }

    /**
     * Register an injector for a particular type.
     * Will automatically inject this type of object when provided in method signatures.
     *
     * @param clazz    Type that the injector should inject for. This type will matched using
     *                 {@link Class#isAssignableFrom(Class)}
     * @param injector The injector that should inject the value into the command method
     * @param <T>      Injected type
     */
    @Override
    public <T> void injector(
            final Class<T> clazz,
            final ParameterInjector<CommandSender, T> injector
    ) {
        this.manager.parameterInjectorRegistry().registerInjector(clazz, injector);
    }

    /**
     * Register an annotation mapper with a constant parameter value
     *
     * @param annotation Annotation class
     * @param parameter  Parameter
     * @param value      Parameter value
     * @param <A>        Annotation type
     * @param <T>        Parameter value type
     */
    @Override
    public <A extends Annotation, T> void annotationParameter(
            final Class<A> annotation,
            final ParserParameter<T> parameter,
            final T value
    ) {
        manager.parserRegistry().registerAnnotationMapper(annotation, (a, typeToken) -> {
            return ParserParameters.single(parameter, value);
        });
    }

    /**
     * Register an annotation mapper with a function to read the parameter value
     * from an annotation
     *
     * @param annotation   Annotation class
     * @param parameter    Parameter
     * @param valueMapper  Mapper from annotation to parameter value
     * @param <A>          Annotation type
     * @param <T>          Parameter value type
     */
    @Override
    public <A extends Annotation, T> void annotationParameter(
            final Class<A> annotation,
            final ParserParameter<T> parameter,
            final Function<A, T> valueMapper
    ) {
        manager.parserRegistry().registerAnnotationMapper(annotation, (a, typeToken) -> {
            return ParserParameters.single(parameter, valueMapper.apply(a));
        });
    }

    /**
     * Register a preprocessor mapper for an annotation.
     * The preprocessor can be created based on properties of the annotation.
     *
     * @param annotation         Annotation class
     * @param preprocessorMapper Preprocessor mapper
     * @param <A>                Annotation type
     */
    @Override
    public <A extends Annotation> void preprocessAnnotation(
            final Class<A> annotation,
            final PreprocessorMapper<A, CommandSender> preprocessorMapper
    ) {
        this.annotationParser.registerPreprocessorMapper(annotation, preprocessorMapper);
    }

    /**
     * Register a preprocessor mapper for an annotation.
     * This assumes the annotation does not store any properties, and the
     * remapper is always constant.
     *
     * @param annotation         Annotation class
     * @param preprocessorMapper Preprocessor mapper
     * @param <A>                Annotation type
     */
    @Override
    public <A extends Annotation> void preprocessAnnotation(
            final Class<A> annotation,
            final ComponentPreprocessor<CommandSender> preprocessorMapper
    ) {
        preprocessAnnotation(annotation, a -> preprocessorMapper);
    }

    /**
     * Registers an exception handler for a given exception class type. This handler will be called
     * when this type of exception is thrown during command handling.
     *
     * @param <T> Exception class type
     * @param exceptionType Type of exception to handle
     * @param handler Handler for the exception type
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T extends Throwable> void handle(Class<T> exceptionType, ThrowingBiConsumer<CommandSender, T> handler) {
        this.exceptionTypes.add(exceptionType);
        this.manager.exceptionController().registerHandler(exceptionType, ctx -> handler.accept(ctx.context().sender(), ctx.exception()));
    }

    /**
     * Registers a message-sending exception handler for a given exception class type. When the exception
     * is handled, the message as specified is sent to the player. If the message matches a caption
     * regex, then the message is first translated.
     *
     * @param <T>
     * @param exceptionType
     * @param message
     */
    @Override
    public <T extends Throwable> void handleMessage(Class<T> exceptionType, String message) {
        final Caption caption = Caption.of(message);
        handle(exceptionType, (sender, exception) -> {
            String translated = manager.captionRegistry().caption(caption, sender);
            sender.sendMessage(Component.text(translated));
        });
    }

    /**
     * Registers a class instance containing command annotations
     *
     * @param <T> Type of annotation holding Object
     * @param annotationsClassInstance Object with command annotations
     */
    @Override
    public <T> void annotations(T annotationsClassInstance) {
        this.annotationParser.parse(annotationsClassInstance);
    }

    /**
     * Registers all the Localization enum constants declared in a Class as captions
     *
     * @param localizationDefaults Enum or Class with static LocalizationEnum constants
     */
    @Override
    public void captionFromLocalization(Class<? extends LocalizationEnum> localizationDefaults) {
        for (LocalizationEnum locale : CommonUtil.getClassConstants(localizationDefaults)) {
            caption(locale.getName(), locale.get().replace("%0%", "{input}"));
        }
    }

    /**
     * Registers a caption with a factory for producing the value for matched captions
     *
     * @param regex The regex to match
     * @param messageFactory Factory for producing the desired value for a caption
     */
    @Override
    public void caption(String regex, Function<CommandSender, String> messageFactory) {
        final Caption caption = Caption.of(regex);
        final CaptionProvider<CommandSender> provider = CaptionProvider.forCaption(caption, messageFactory);
        manager.captionRegistry().registerProvider(provider);
    }

    /**
     * Registers a caption with a fixed value String
     *
     * @param regex The regex to match
     * @param value The String value to use when the regex is matched
     */
    @Override
    public void caption(String regex, String value) {
        caption(regex, sender -> value);
    }

    /**
     * Registers a new help command for all the commands under a filter prefix
     *
     * @param filterPrefix Command filter prefix, for commands shown in the menu
     * @param helpDescription Description of the help command
     * @return minecraft help command
     */
    @Override
    public Command<CommandSender> helpCommand(
            List<String> filterPrefix,
            String helpDescription
    ) {
        return helpCommand(filterPrefix, helpDescription, builder -> builder);
    }

    /**
     * Registers a new help command for all the commands under a filter prefix
     *
     * @param filterPrefix Command filter prefix, for commands shown in the menu
     * @param helpDescription Description of the help command
     * @param modifier Modifier for the command applied before registering
     * @return minecraft help command
     */
    @Override
    public Command<CommandSender> helpCommand(
            List<String> filterPrefix,
            String helpDescription,
            Function<Command.Builder<CommandSender>, Command.Builder<CommandSender>> modifier
    ) {
        String helpCmd = "/" + String.join(" ", filterPrefix) + " help";
        final MinecraftHelp<CommandSender> help = this.help(helpCmd, filterPrefix);

        // Start a builder
        Command.Builder<CommandSender> command = Command.newBuilder(
                filterPrefix.get(0),
                CommandMeta.empty());
        command = command.commandDescription(Description.of(helpDescription));

        // Add literals, then 'help'
        for (int i = 1; i < filterPrefix.size(); i++) {
            command = command.literal(filterPrefix.get(i));
        }
        command = command.literal("help");
        command = command.optional("query", StringParser.greedyStringParser());
        command = command.handler(context -> {
            String query = context.getOrDefault("query", "");
            help.queryCommands(query, context.sender());
        });
        command = modifier.apply(command);

        // Build & return
        Command<CommandSender> builtCommand = command.build();
        this.manager.command(builtCommand);
        return builtCommand;
    }

    /**
     * Creates a help menu
     *
     * @param commandPrefix Help command prefix
     * @param filterPrefix Command filter prefix, for commands shown in the menu
     * @return minecraft help
     */
    @Override
    public MinecraftHelp<CommandSender> help(String commandPrefix, final List<String> filterPrefix) {
        MinecraftHelp<CommandSender> help = MinecraftHelp.<CommandSender>builder()
                .commandManager(this.manager)
                .audienceProvider((sender -> AudienceUtil.Bukkit.audiences.sender(((BukkitCommandSender) sender).getSender())))
                .commandPrefix(commandPrefix)
                .commandFilter(command -> {
                    List<CommandComponent<CommandSender>> args = command.components();
                    if (args.size() < filterPrefix.size()) {
                        return false;
                    }
                    for (int i = 0; i < filterPrefix.size(); i++) {
                        if (!args.get(i).name().equals(filterPrefix.get(i))) {
                            return false;
                        }
                    }
                    return true;
                })
                .build();

        return help;
    }
}