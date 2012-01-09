// This file was automatically generated from login.soy.
// Please don't edit this file by hand.

package org.koshinuke.soy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.template.soy.parseinfo.SoyFileInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo;
import com.google.template.soy.parseinfo.SoyTemplateInfo.ParamRequisiteness;


/**
 * Soy parse info for login.soy.
 */
public class LoginSoyInfo extends SoyFileInfo {


  public static class Param {
    private Param() {}

  }


  /**
   * login form
   */
  public static final SoyTemplateInfo LOGINFORM = new SoyTemplateInfo(
      "org.koshinuke.login.loginform",
      ImmutableMap.<String, ParamRequisiteness>of(),
      ImmutableSortedSet.<String>of());


  private LoginSoyInfo() {
    super("login.soy",
          "org.koshinuke.login",
          ImmutableSortedSet.<String>of(),
          ImmutableList.<SoyTemplateInfo>of(
              LOGINFORM),
          ImmutableMap.<String, CssTagsPrefixPresence>of());
  }


  private static final LoginSoyInfo __INSTANCE__ = new LoginSoyInfo();


  public static LoginSoyInfo getInstance() {
    return __INSTANCE__;
  }

}
