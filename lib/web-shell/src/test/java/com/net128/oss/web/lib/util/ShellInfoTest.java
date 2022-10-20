package com.net128.oss.web.lib.util;

import com.net128.oss.web.webshell.util.ShellInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShellInfoTest {
    @Test
    public void testShell() {
        Assertions.assertNotNull(ShellInfo.determineShellInfo("zsh"));
    }
}
