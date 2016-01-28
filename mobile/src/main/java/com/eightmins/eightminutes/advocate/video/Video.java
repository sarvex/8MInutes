package com.eightmins.eightminutes.advocate.video;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by nabhilax on 23/01/16.
 */
@ParseClassName("Video")
public class Video extends ParseObject {

  public void load(String name, String description, String url) {

    put("name", name);
    put("description", description);
    put("url", url);
  }

  public String getName() {
    return getString("name");
  }

  public void setName(String name) {
    put("name", name);
  }

  public String getDescription() {
    return getString("description");
  }

  public void setDescription(String description) {
    put("description", description);
  }

  public String getUrl() {
    return getString("id");
  }

  public void setUrl(String id) {
    put("id", id);
  }

}
