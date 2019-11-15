package kr.pe.tocgic.tools.strresparser.view.data;

import com.google.gson.reflect.TypeToken;
import kr.pe.tocgic.tools.strresparser.util.JsonUtil;
import kr.pe.tocgic.tools.strresparser.util.StringUtil;
import kr.pe.tocgic.tools.strresparser.view.data.model.StringPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EnvProperty {
    private final String KEY_PATH_OUT = "pathOut";
    private final String KEY_PATH_STRINGS = "pathStrings";

    private String appConfigPath;

    private Properties appProps;

    private String outPath;
    private List<StringPath> stringPaths;

    public EnvProperty() {
        init();
        this.outPath = getProperty(KEY_PATH_OUT, "");
        String pathStrings = getProperty(KEY_PATH_STRINGS, null);
        if (pathStrings != null) {
            stringPaths = JsonUtil.fromJson(pathStrings, new TypeToken<ArrayList<StringPath>>(){}.getType());
        }
        if (stringPaths == null) {
            stringPaths = new ArrayList<>();
        }
    }

    private void init() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        final String rootPath = url != null ? url.getPath() : File.separator;
        appConfigPath = rootPath + "strresparser.properties";

        appProps = new Properties();
        try {
            if (!new File(appConfigPath).exists()) {
                storeProperty();
            }
            appProps.load(new FileInputStream(appConfigPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getOutPath() {
        return outPath;
    }

    public void setOutPath(String outPath) {
        boolean needUpdate = false;
        if (StringUtil.isEmpty(outPath)) {
            if (StringUtil.isNotEmpty(this.outPath)) {
                needUpdate = true;
            }
        } else {
            if (!outPath.equals(this.outPath)) {
                needUpdate = true;
            }
        }
        if (needUpdate) {
            this.outPath = outPath;
            setProperty(KEY_PATH_OUT, outPath);
        }
    }

    public boolean addStrPathItem(StringPath item) throws Exception {
        if (stringPaths.contains(item)) {
            throw new Exception("이미 등록된 경로입니다.");
        }
        stringPaths.add(item);
        setProperty(KEY_PATH_STRINGS, JsonUtil.toJson(stringPaths));
        return true;
    }

    public boolean removeStrPathItem(StringPath item) {
        boolean isChanged = false;
        if (stringPaths.contains(item)) {
            stringPaths.remove(item);
            isChanged = true;
        }
        if (isChanged) {
            setProperty(KEY_PATH_STRINGS, JsonUtil.toJson(stringPaths));
        }
        return true;
    }

    public List<StringPath> getStringPaths() {
        return new ArrayList<>(stringPaths);
    }

    private void setProperty(String key, String value) {
        appProps.setProperty(key, value);
        storeProperty();
    }

    private String getProperty(String key, String defaultValue) {
        return appProps.getProperty(key, defaultValue);
    }

    private void storeProperty() {
        try {
            appProps.store(new FileWriter(appConfigPath), "store to properties file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
