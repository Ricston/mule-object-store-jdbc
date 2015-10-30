package com.ricston.jdbc.objectstore;

import java.io.Serializable;

import org.mule.api.store.ObjectStoreException;
import org.mule.api.transformer.Transformer;
import org.mule.api.transformer.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformableJdbcObjectStore extends JdbcListableObjectStore<Serializable> {

	private static final Logger LOG = LoggerFactory.getLogger(TransformableJdbcObjectStore.class);

	protected Transformer transformer;

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	@Override
	public Serializable retrieve(Serializable key) throws ObjectStoreException {
		Serializable res = super.retrieve(key);
		if (transformer == null) {
			return res;
		} else {
			try {
				return (Serializable) transformer.transform(res);
			} catch (TransformerException e) {
				e.printStackTrace();
				LOG.warn("Failed to transform result of object store retrieval to a Serializable. Returning the Serializable retrieved from object store without any transformation");
				return res;
			}
		}
	}
}
