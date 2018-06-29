package io.ebean.tools.init.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QuestionOptions {

  private Map<String,String> opts = new LinkedHashMap<>();

  private int keyStart;

  public void add(String key, String text) {
    opts.put(key.toUpperCase(), text);

    try {
      keyStart = Math.max(keyStart, Integer.parseInt(key));
    } catch (NumberFormatException e) {
      // ignore
    }
  }

  public void addAll(List<String> list) {
    for (int i = keyStart; i < list.size(); i++) {
      add(""+(i + 1), list.get(i));
    }
  }

  public String selected(String key) {
    return opts.get(key.toUpperCase());
  }

  public Set<Map.Entry<String, String>> entries() {
    return opts.entrySet();
  }

  public Set<String> keys() {
    return opts.keySet();
  }
}
