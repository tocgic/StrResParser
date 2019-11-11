package kr.pe.tocgic.tools.strresparser.util;

import java.io.File;

public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    /**
     * 변경된(source) 파일의 이름을 변경 (target)로 적용한다.
     * @param target
     * @param source
     * @return
     */
    public static boolean renameFile(File source, File target, boolean removeSource) {
        boolean result;
        //원본 -> 원본.시간.bak
        //사본 -> 원본
        //
        File originBak = new File(target.getAbsolutePath() + "." + System.currentTimeMillis() + ".bak");
        result = target.renameTo(originBak);
        if (result) {
            result = source.renameTo(target);
            if (result) {
                boolean ret = originBak.delete();
                Logger.d(TAG, ">>>> file done : originBak.delete():" + ret);
            } else {
                boolean ret = originBak.renameTo(target);
                Logger.d(TAG, ">>>> file rollback : originBak.renameTo(target):" + ret);
            }
        } else {
            boolean ret = false;
            if (removeSource) {
                ret = source.delete();
            }
            Logger.d(TAG, ">>>> target file rollback : source.delete():" + ret);
        }
        return result;
    }
}
