package kr.pe.tocgic.tools.strresparser;

import kr.pe.tocgic.tools.strresparser.data.enums.Language;

public class StResParser {
    public static void main(String[] args) {
        StResManager manager = new StResManager();
        manager.addResourcePath(Language.KO, "/Users/tocgic/Documents/_Temp/agent-string-resources/");
    }
}
