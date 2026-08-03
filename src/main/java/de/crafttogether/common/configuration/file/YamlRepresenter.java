package de.crafttogether.common.configuration.file;

import de.crafttogether.common.configuration.ConfigurationSection;
import de.crafttogether.common.configuration.serialization.ConfigurationSerializable;
import de.crafttogether.common.configuration.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Representer;

import java.util.LinkedHashMap;
import java.util.Map;

public class YamlRepresenter extends Representer {

    public YamlRepresenter() {
        this(new DumperOptions());
    }

    public YamlRepresenter(DumperOptions dumperOptions) {
        super(dumperOptions);

        this.multiRepresenters.put(
                ConfigurationSection.class,
                new RepresentConfigurationSection()
        );

        this.multiRepresenters.put(
                ConfigurationSerializable.class,
                new RepresentConfigurationSerializable()
        );
    }

    private class RepresentConfigurationSection extends RepresentMap {

        @Override
        public Node representData(Object data) {
            ConfigurationSection section = (ConfigurationSection) data;
            return super.representData(section.getValues(false));
        }
    }

    private class RepresentConfigurationSerializable extends RepresentMap {

        @Override
        public Node representData(Object data) {
            ConfigurationSerializable serializable =
                    (ConfigurationSerializable) data;

            Map<String, Object> values = new LinkedHashMap<>();

            values.put(
                    ConfigurationSerialization.SERIALIZED_TYPE_KEY,
                    ConfigurationSerialization.getAlias(serializable.getClass())
            );

            values.putAll(serializable.serialize());

            return super.representData(values);
        }
    }
}