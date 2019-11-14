package kr.pe.tocgic.tools.functions;

import kr.pe.tocgic.tools.data.ResourceDataManager;

import java.io.File;

public interface IResourceTransform {
    boolean exportFile(ResourceDataManager source, File target);
    boolean importFile(File source, ResourceDataManager target);
}
