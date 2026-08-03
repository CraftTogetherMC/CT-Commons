package de.crafttogether.common.configuration.file;

import de.crafttogether.common.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlConstructor extends SafeConstructor {

    public YamlConstructor() {
        this(new LoaderOptions());
    }

    public YamlConstructor(LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }

    private class ConstructCustomObject extends ConstructYamlMap {

        @Override
        public Object construct(Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException(
                        "Unexpected referential mapping structure. Node: " + node
                );
            }

            Map<?, ?> raw = (Map<?, ?>) super.construct(node);

            if (!raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                return raw;
            }

            Map<String, Object> typed = new LinkedHashMap<>(raw.size());

            for (Map.Entry<?, ?> entry : raw.entrySet()) {
                typed.put(String.valueOf(entry.getKey()), entry.getValue());
            }

            try {
                return ConfigurationSerialization.deserializeObject(typed);
            } catch (IllegalArgumentException exception) {
                throw new YAMLException(
                        "Could not deserialize object",
                        exception
                );
            }
        }

        @Override
        public void construct2ndStep(Node node, Object object) {
            throw new YAMLException(
                    "Unexpected referential mapping structure. Node: " + node
            );
        }
    }
}