package de.crafttogether.common.cloud;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.annotations.injection.ParameterInjector;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameter;
import cloud.commandframework.arguments.parser.ParserParameters;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import de.crafttogether.common.localization.LocalizationEnum;
import io.leangen.geantyref.TypeToken;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CloudSimpleHandler {
    boolean isEnabled();

    void enable();

    void handleException(CommandSender sender, Throwable exception);

    AnnotationParser<CommandSender> getParser();

    CommandManager<CommandSender> getManager();

    void postProcess(CommandPostprocessor<CommandSender> processor);

    <T> void parse(
            Class<T> type,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    );

    <T> void parse(
            TypeToken<T> type,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    );

    void parse(
            String name,
            Function<ParserParameters, ArgumentParser<CommandSender, ?>> supplier
    );

    void suggest(String name, List<String> suggestions);

    void suggest(String name, Supplier<List<String>> suggestionsProvider);

    void suggest(
            String name,
            BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider
    );

    <T> void inject(
            Class<T> clazz,
            T value
    );

    <T> void injector(
            Class<T> clazz,
            ParameterInjector<CommandSender, T> injector
    );

    <A extends Annotation, T> void annotationParameter(
            Class<A> annotation,
            ParserParameter<T> parameter,
            T value
    );

    <A extends Annotation, T> void annotationParameter(
            Class<A> annotation,
            ParserParameter<T> parameter,
            Function<A, T> valueMapper
    );

    <A extends Annotation> void preprocessAnnotation(
            Class<A> annotation,
            Function<A, BiFunction<CommandContext<CommandSender>, Queue<String>,
                    ArgumentParseResult<Boolean>>> preprocessorMapper
    );

    <A extends Annotation> void preprocessAnnotation(
            Class<A> annotation,
            BiFunction<CommandContext<CommandSender>, Queue<String>,
                    ArgumentParseResult<Boolean>> preprocessorMapper
    );

    @SuppressWarnings({"rawtypes", "unchecked"})
    <T extends Throwable> void handle(Class<T> exceptionType, BiConsumer<CommandSender, T> handler);

    <T extends Throwable> void handleMessage(Class<T> exceptionType, String message);

    <T> void annotations(T annotationsClassInstance);

    void captionFromLocalization(Class<? extends LocalizationEnum> localizationDefaults);

    void caption(String regex, BiFunction<Caption, CommandSender, String> messageFactory);

    void caption(String regex, String value);

    Command<CommandSender> helpCommand(
            List<String> filterPrefix,
            String helpDescription
    );

    Command<CommandSender> helpCommand(
            List<String> filterPrefix,
            String helpDescription,
            Function<Command.Builder<CommandSender>, Command.Builder<CommandSender>> modifier
    );

    MinecraftHelp<CommandSender> help(String commandPrefix, List<String> filterPrefix);
}
