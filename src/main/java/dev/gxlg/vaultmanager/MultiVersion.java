package dev.gxlg.vaultmanager;

import net.minecraft.util.ActionResult;

import java.util.Objects;

public class MultiVersion {
    public static boolean wasKeyUsed(ActionResult result) {
        if (Reflection.version(">= 1.21.2")) {
            return Objects.equals(result, Reflection.wrap("ActionResult:null field_52422/SUCCESS_SERVER"));
        } else {
            return Objects.equals(result, Reflection.wrap("ActionResult:null field_21466/CONSUME"));
        }
    }
}
