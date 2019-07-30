package io.ebean.tools.init.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionOptions {

  private Map<String,Option> opts = new LinkedHashMap<>();

  public void add(String key, String text, String description) {

    Option option = new Option(key, text, description);
    opts.put(option.key, option);
  }

  public void addAll(List<String> list) {
    for (int i = 0; i < list.size(); i++) {
      add(Integer.toString(i), list.get(i), null);
    }
  }

  public Option selected(String key) {
    return opts.get(key.toUpperCase());
  }

  public Set<Map.Entry<String, Option>> entries() {
    return opts.entrySet();
  }

  public Set<String> keys() {
    return opts.keySet();
  }

  public static class Option {

    public String key;
    public String text;
    public String description;

    public Option(String key, String text, String description) {
      this.key = key.toUpperCase();
      this.text = text;
      this.description = description;
    }

    @Override
    public String toString() {
      return text;
    }
  }
}
