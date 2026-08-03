package de.crafttogether.common.commands;

import de.crafttogether.common.localization.LocalizationEnum;
import io.leangen.geantyref.TypeToken;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.PreprocessorMapper;
import org.incendo.cloud.component.preprocessor.ComponentPreprocessor;
import org.incendo.cloud.execution.postprocessor.CommandPostprocessor;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.parser.ParserParameter;
import org.incendo.cloud.parser.ParserParameters;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface CloudSimpleHandler {
    boolean isEnabled();

    void enable();

    void handleException(CommandSender sender, Throwable exception) throws Throwable;

    AnnotationParser<CommandSender> getParser();

    CommandManager<CommandSender> getManager();

    void postProcess(CommandPostprocessor<CommandSender> processor);

    <T> void parse(ParserDescriptor<CommandSender, T> descriptor);

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
            BlockingSuggestionProvider.Strings<CommandSender> suggestionsProvider
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
            PreprocessorMapper<A, CommandSender> preprocessorMapper
    );

    <A extends Annotation> void preprocessAnnotation(
            Class<A> annotation,
            ComponentPreprocessor<CommandSender> preprocessorMapper
    );

    @SuppressWarnings({"rawtypes", "unchecked"})
    <T extends Throwable> void handle(Class<T> exceptionType, ThrowingBiConsumer<CommandSender, T> handler);

    <T extends Throwable> void handleMessage(Class<T> exceptionType, String message);

    <T> void annotations(T annotationsClassInstance);

    void captionFromLocalization(Class<? extends LocalizationEnum> localizationDefaults);

    void caption(String regex, Function<CommandSender, String> messageFactory);

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
