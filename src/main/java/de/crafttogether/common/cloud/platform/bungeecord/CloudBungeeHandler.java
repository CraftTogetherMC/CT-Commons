package de.crafttogether.common.cloud.platform.bungeecord;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.injection.ParameterInjector;
import cloud.commandframework.arguments.parser.*;
import cloud.commandframework.bungee.BungeeCommandManager;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.SimpleCaptionRegistry;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.CommandExecutionException;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.services.PipelineException;
import de.crafttogether.common.cloud.CloudCommandPreprocessor;
import de.crafttogether.common.cloud.CloudLocalizedException;
import de.crafttogether.common.cloud.CloudSimpleHandler;
import de.crafttogether.common.cloud.CommandSender;
import de.crafttogether.common.localization.LocalizationEnum;
import de.crafttogether.common.util.CommonUtil;
import de.crafttogether.ctcommons.CTCommonsBungee;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.plugin.Plugin;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Configures the Cloud Command Framework for basic use inside a Bungeecord or
 * Waterfall server environment. Initializes the command manager itself as synchronously
 * executing (on main thread), registers annotations and help system, and also
 * registers some useful preprocessing logic.
 *
 * From annotation-registered commands you can obtain your plugin instance easily,
 * as it is made available through an injector by default.
 */
public class CloudBungeeHandler implements CloudSimpleHandler {
    private BungeeCommandManager<CommandSender> manager;
    private AnnotationParser<CommandSender> annotationParser;

    @Override
    public boolean isEnabled() {
        return false;
    }

    /**
     * Enables and initializes the Cloud Command Framework. After this is
     * called, commands and other things can be registered.
     *
     * @param plugin Owning Bungeecord Plugin for this handler
     */

    public void enable(Plugin plugin) {
        try {
            this.manager = new BungeeCommandManager<>(
                    /* Owning plugin */ plugin,
                    /* Coordinator function */ CommandExecutionCoordinator.simpleCoordinator(),
                    /* Command Sender -> C */ CloudBungeeHandler::getCommandSender,
                    /* C -> Command Sender */ (sender) -> ((BungeeCommandSender) sender).getSender()
            );
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to initialize the command manager", e);
        }


        // Registers a custom command preprocessor that handles quote-escaping
        this.manager.registerCommandPreProcessor(new CloudCommandPreprocessor());

        // Create the annotation parser. This allows you to define commands using methods annotated with
        // @CommandMethod
        final Function<ParserParameters, CommandMeta> commandMetaFunction = p ->
                CommandMeta.simple()
                        .with(CommandMeta.DESCRIPTION, p.get(StandardParameters.DESCRIPTION, "No description"))
                        .build();
        this.annotationParser = new AnnotationParser<>(
                /* Manager */ this.manager,
                /* Command sender type */ CommandSender.class,
                /* Mapper for command meta instances */ commandMetaFunction
        );

        // Shows the argname as <argname> as a suggestion
        // Fix for numeric arguments on the broken brigadier system
        suggest("argname", (context,b) -> Collections.singletonList("<" + context.getCurrentArgument().getName() + ">"));

        // Fixes incorrect exception handling in Cloud, so that user-specified
        // exception types can be used instead.
        handle(CommandExecutionException.class, this::handleException);
        handle(PipelineException.class, this::handleException);

        // Makes LocalizedParserException functional
        handle(CloudLocalizedException.class, (sender, ex) -> {
            sender.sendMessage(Component.text(ex.getMessage()));
        });
    }

    public void enable() {
        if (isEnabled())
            return;

        enable(CTCommonsBungee.plugin);
    }

    private static CommandSender getCommandSender(net.md_5.bungee.api.CommandSender bungeeCommandSender) {
        return new BungeeCommandSender(bungeeCommandSender);
    }

    @Override
    public void handleException(CommandSender sender, Throwable exception) {
        Throwable cause = exception.getCause();

        // Find handler for this exception, if registered, execute that handler
        // If the handler throws, handle it as an internal error
        @SuppressWarnings({"unchecked", "rawtypes"})
        BiConsumer<CommandSender, Throwable> handler = manager.getExceptionHandler((Class) cause.getClass());
        if (handler != null) {
            try {
                handler.accept(sender, exception.getCause());
                return;
            } catch (Throwable t2) {
                cause = t2;
            }
        }

        // Default fallback
        this.manager.getOwningPlugin().getLogger().log(Level.SEVERE,
                "Exception executing command handler", cause);
        sender.sendMessage(Component.text("An internal error occurred while attempting to perform this command.").color(NamedTextColor.RED));
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
     * Register a parser supplier
     *
     * @param type     The type that is parsed by the parser
     * @param supplier The function that generates the parser. The map supplied may contain parameters used
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
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
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
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
     *                 to configure the parser, many of which are documented in {@link StandardParameters}
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
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider
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
            final Function<A, BiFunction<CommandContext<CommandSender>, Queue<String>,
                    ArgumentParseResult<Boolean>>> preprocessorMapper
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
            final BiFunction<CommandContext<CommandSender>, Queue<String>,
                    ArgumentParseResult<Boolean>> preprocessorMapper
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
    public <T extends Throwable> void handle(Class<T> exceptionType, BiConsumer<CommandSender, T> handler) {
        this.manager.registerExceptionHandler((Class) exceptionType, (BiConsumer) handler);
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
            String translated = manager.captionRegistry().getCaption(caption, sender);
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
    public void caption(String regex, BiFunction<Caption, CommandSender, String> messageFactory) {
        if (manager.captionRegistry() instanceof SimpleCaptionRegistry) {
            final Caption caption = Caption.of(regex);
            ((SimpleCaptionRegistry<CommandSender>) manager.captionRegistry()).registerMessageFactory(
                    caption, messageFactory
            );
        }
    }

    /**
     * Registers a caption with a fixed value String
     *
     * @param regex The regex to match
     * @param value The String value to use when the regex is matched
     */
    @Override
    public void caption(String regex, String value) {
        caption(regex, (caption, sender) -> value);
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

    @Override
    public Command<CommandSender> helpCommand(List<String> filterPrefix, String helpDescription, Function<Command.Builder<CommandSender>, Command.Builder<CommandSender>> modifier) {
        return null;
    }

    @Override
    public MinecraftHelp<CommandSender> help(String commandPrefix, List<String> filterPrefix) {
        return null;
    }

}