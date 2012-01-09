// This file was automatically generated from repo.soy.
// Please don't edit this file by hand.

package org.koshinuke.soy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.template.soy.parseinfo.SoyFileInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo.ParamRequisiteness;


/**
 * Soy parse info for repo.soy.
 */
public class RepoSoyInfo extends SoyFileInfo {


  public static class Param {
    private Param() {}

  }


  /**
   * Home
   */
  public static final SoyTemplateInfo HOME = new SoyTemplateInfo(
      "org.koshinuke.repository.home",
      ImmutableMap.<String, ParamRequisiteness>of(),
      ImmutableSortedSet.<String>of());


  private RepoSoyInfo() {
    super("repo.soy",
          "org.koshinuke.repository",
          ImmutableSortedSet.<String>of(),
          ImmutableList.<SoyTemplateInfo>of(
              HOME),
          ImmutableMap.<String, CssTagsPrefixPresence>of());
  }


  private static final RepoSoyInfo __INSTANCE__ = new RepoSoyInfo();


  public static RepoSoyInfo getInstance() {
    return __INSTANCE__;
  }

}
