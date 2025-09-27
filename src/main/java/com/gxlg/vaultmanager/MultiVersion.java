package com.gxlg.vaultmanager;

import net.minecraft.util.ActionResult;

public class MultiVersion {
    public static boolean wasKeyUsed(ActionResult result) {
        if (Reflection.version(">= 1.21.2")) {
            return result == Reflection.wrap("ActionResult:null field_52422/SUCCESS_SERVER");
        } else {
            return result == Reflection.wrap("ActionResult:null field_21466/CONSUME");
        }
    }
}
