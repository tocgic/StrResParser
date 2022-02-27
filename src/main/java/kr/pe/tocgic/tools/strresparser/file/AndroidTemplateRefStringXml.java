package kr.pe.tocgic.tools.strresparser.file;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * com.likethesalad.android:string-reference:1.3.0 (placeholder-resolver)
 * - android 의 string resource 를 참조하여 사용할 수 있도록 지원해주는 openSource
 * - 플러그인이, 빌드 타임에 template_ 로 시작하는 리소스 ID (template_description) 에 대해서, 참조리소스를 반영한 ID 를 새로 생성 (R.string.description) 하여, 적용 가능
 * - 단점 : AndroidStudio 에서 리소스가 없다는 오류 표시
 *
 * 단점을 보완하기 위한 더미 리소스파일 생성
 *
 * </resources>
 */
public class AndroidTemplateRefStringXml {
    private static final String TEMPLATE_KEY = "template_";
    private static final String TEMPLATE_START = "<?xml version='1.0' encoding='UTF-8'?>\n<resources xmlns:tools=\"http://schemas.android.com/tools\">\n";
    private static final String TEMPLATE_BODY = "    <string name=\"%s\" translatable=\"false\">@string/%s</string>\n";
    private static final String TEMPLATE_END = "</resources>\n";

    private final File dest;

    public AndroidTemplateRefStringXml(@Nonnull File dest) {
        this.dest = dest;
    }

    public void generate(@Nonnull List<String> keyList) throws Exception {
        if (dest.exists()) {
            if (!dest.delete()) {
                throw new Exception("dest 파일 을 생성할 수 없음");
            }
        }

        List<String> templatedKeyList = keyList.stream().filter(key -> key.startsWith(TEMPLATE_KEY)).sorted().collect(Collectors.toList());
        if (templatedKeyList.size() < 1) {
            return;
        }

        if (!dest.createNewFile()) {
            throw new Exception("dest 파일 을 생성할 수 없음");
        }

        try(FileWriter fw = new FileWriter(dest);
            BufferedWriter writer = new BufferedWriter(fw)
        ) {
            writer.write(TEMPLATE_START);
            for (String templateKey : templatedKeyList) {
                String newKey = templateKey.replace(TEMPLATE_KEY, "");
                writer.write(String.format(TEMPLATE_BODY, newKey, templateKey));
            }
            writer.write(TEMPLATE_END);
            writer.flush();
        }
    }
}
