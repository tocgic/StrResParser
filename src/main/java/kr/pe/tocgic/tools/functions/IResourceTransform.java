package kr.pe.tocgic.tools.functions;

import kr.pe.tocgic.tools.data.ResourceModelList;

import java.io.File;

public interface IResourceTransform {
    boolean exportFile(ResourceModelList source, File target);
    boolean importFile(File source, ResourceModelList target);
}
