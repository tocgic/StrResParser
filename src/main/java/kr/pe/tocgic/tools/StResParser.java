package kr.pe.tocgic.tools;

import kr.pe.tocgic.tools.data.enums.Language;

public class StResParser {
    public static void main(String[] args) {
        StResManager manager = new StResManager();
        manager.setResourceDirectory(Language.KO, "/Users/tocgic/Documents/_Temp/agent-string-resources/");
    }
}
