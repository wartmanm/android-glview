package com.github.wartman4404.glview.material;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface MaterialLoader {
	public InputStream getMaterialStream(String name) throws FileNotFoundException, IOException;
}
