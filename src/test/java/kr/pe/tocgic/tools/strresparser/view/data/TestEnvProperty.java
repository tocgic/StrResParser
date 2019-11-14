package kr.pe.tocgic.tools.strresparser.view.data;

import kr.pe.tocgic.tools.strresparser.data.enums.Language;
import kr.pe.tocgic.tools.strresparser.view.data.model.StringPath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestEnvProperty {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * setOut
     */
    @Test
    public void setOutPath() {
        EnvProperty prop = new EnvProperty();
        String outPath = "/test/out/path";
        prop.setOutPath(outPath);

        assertEquals (outPath, prop.getOutPath());

        prop = new EnvProperty();
        assertEquals (outPath, prop.getOutPath());

        prop.setOutPath("");
    }

    @Test
    public void removeAllStringPathList() {
        EnvProperty prop = new EnvProperty();
        List<StringPath> list = prop.getStringPaths();
        for (StringPath item : list) {
            prop.removeStrPathItem(item);
        }
        assert (getCountOfStringPathList() == 0);
    }

    /**
     * StringPath 에 대한 addStrPathItem(), removeStrPathIte() 동작 확인
     * 1. 10개의 item 을 추가
     * 2. new EnvProperty() 후, 10개 등록 갯수 확인
     * 3. 10번째 item "/test/out/path/9" 제거
     * 4. new EnvProperty() 후, 9개 등록 갯수 확인
     * 5, 중복 item "/test/out/path/0" 등록 => Exception "이미 등록된 경로입니다." 발생
     * 6. new EnvProperty() 후, 9개 등록 갯수 확인
     * 7. 존제 하지 않는 item "/test/out/path/9" 제거
     * 8. new EnvProperty() 후, 9개 등록 갯수 확인
     * 9. 초기화 : 저장된 item 제거
     */
    @Test
    public void addStrPathItem() throws Exception {
        assert (getCountOfStringPathList() == 0);

        String strPath = "/test/out/path/";
        EnvProperty prop = new EnvProperty();

        for (int i = 0; i < 10; i++) {
            prop.addStrPathItem(new StringPath(Language.KO, strPath + i));
        }

        assert (getCountOfStringPathList() == 10);

        prop = new EnvProperty();
        prop.removeStrPathItem(new StringPath(Language.KO, strPath + 9));

        assert (getCountOfStringPathList() == 9);

        expectedException.expect(Exception.class);
        expectedException.expectMessage("이미 등록된 경로입니다.");
        prop = new EnvProperty();
        prop.addStrPathItem(new StringPath(Language.KO, strPath + 0));

        assert (getCountOfStringPathList() == 9);

        prop = new EnvProperty();
        prop.removeStrPathItem(new StringPath(Language.KO, strPath + 9));

        assert (getCountOfStringPathList() == 9);

        removeAllStringPathList();
    }

    private int getCountOfStringPathList() {
        EnvProperty prop = new EnvProperty();
        List list = prop.getStringPaths();
        return list.size();
    }
}
