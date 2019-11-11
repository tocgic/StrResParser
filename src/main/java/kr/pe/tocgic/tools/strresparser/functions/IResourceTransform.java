package kr.pe.tocgic.tools.strresparser.functions;

import kr.pe.tocgic.tools.strresparser.data.ResourceDataManager;

import java.io.File;

public interface IResourceTransform {
    boolean exportFile(ResourceDataManager source, File target);
    boolean importFile(File source, ResourceDataManager target);
}
