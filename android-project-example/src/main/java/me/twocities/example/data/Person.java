package me.twocities.example.data;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

@AutoValue public abstract class Person {

  public abstract String name();

  public abstract String id();

  public abstract int gender();

  public static TypeAdapter<Person> typeAdapter(Gson gson) {
    return new AutoValue_Person.GsonTypeAdapter(gson);
  }
}