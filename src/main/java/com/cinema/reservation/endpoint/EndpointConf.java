package com.cinema.reservation.endpoint;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.cinema.reservation.PojaGenerated;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@PojaGenerated
@Configuration
public class EndpointConf {
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.findAndRegisterModules();
    objectMapper.registerModule(javaTimeModule());
    return objectMapper;
  }

  private SimpleModule javaTimeModule() {
    return new SimpleModule()
        .addSerializer(
            Instant.class,
            new JsonSerializer<Instant>() {
              @Override
              public void serialize(Instant value, JsonGenerator gen, SerializerProvider sp)
                  throws IOException {
                gen.writeString(value.toString());
              }
            })
        .addDeserializer(
            Instant.class,
            new JsonDeserializer<Instant>() {
              @Override
              public Instant deserialize(JsonParser p, DeserializationContext ctx)
                  throws IOException {
                return Instant.parse(p.getText());
              }
            })
        .addSerializer(
            Duration.class,
            new JsonSerializer<Duration>() {
              @Override
              public void serialize(Duration value, JsonGenerator gen, SerializerProvider sp)
                  throws IOException {
                gen.writeString(value.toString());
              }
            })
        .addDeserializer(
            Duration.class,
            new JsonDeserializer<Duration>() {
              @Override
              public Duration deserialize(JsonParser p, DeserializationContext ctx)
                  throws IOException {
                return Duration.parse(p.getText());
              }
            });
  }
}
