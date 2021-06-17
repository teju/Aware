package com.iapps.libs.objects;

public class BeanType
	extends SimpleBean {

	private String typeId;

	public BeanType(int id, String name) {
		super(id, name);
	}

	public BeanType(int id, String name, String typeId) {
		super(id, name);
		setTypeId(typeId);
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String type_id) {
		this.typeId = type_id;
	}

}
