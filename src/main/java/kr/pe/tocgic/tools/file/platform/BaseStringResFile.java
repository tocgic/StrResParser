package kr.pe.tocgic.tools.file.platform;

import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BaseStringResFile {
    protected String TAG = getClass().getSimpleName();
    protected char NODE_TOKEN = '"';

    protected boolean isValidFile(File source) {
        if (source == null) {
            Logger.w(TAG, "sourceFilePath is Null");
            return false;
        }
        if (!source.exists() || !source.isFile()) {
            Logger.w(TAG, "file NOT exist or NOT File");
            return false;
        }
        return true;
    }

    protected String getStringFromFile(String jsonFilePath) {
        String jsonString = null;
        if (jsonFilePath != null && jsonFilePath.length() > 0) {
            File jsonFile = new File(jsonFilePath);
            BufferedReader streamReader = null;
            try {
                streamReader = new BufferedReader(new FileReader(jsonFile));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr = null;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                }
                jsonString = responseStrBuilder.toString();
            } catch (Exception e) {
                Logger.w(TAG, e.getMessage());
            } finally {
                try {
                    if (streamReader != null) {
                        streamReader.close();
                    }
                } catch (Exception eFile) {
                    Logger.w(TAG, eFile.getMessage());
                }
            }
        }
        return jsonString;
    }

    protected int getStringNode(String line, int startIndex, StringBuilder target) {
        if (StringUtil.isNull(line)) {
            return startIndex;
        }
        if (target == null) {
            return startIndex;
        }
        char preChar = 0;
        boolean isStart = false;
        int index = startIndex;
        int lineLength = line.length();
        while (index < lineLength) {
            char ch = line.charAt(index++);
            if (ch == NODE_TOKEN) {
                if (!isStart) {
                    isStart = true;
                    continue;
                } else {
                    if (preChar != '\\') {
                        break;
                    }
                }
            }
            if (isStart) {
                target.append(ch);
            }
            preChar = ch;
        }
        return index;
    }

    protected boolean isSkipLine(String line) {
        return StringUtil.isEmpty(line);
    }
}
