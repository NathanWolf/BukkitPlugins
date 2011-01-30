package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PersistedReference extends PersistedField
{

	public PersistedReference(Field field)
	{
		super(field);
	}
	
	public PersistedReference(Method getter, Method setter)
	{
		super(getter, setter);
	}
	
	@Override
	public void bind()
	{
		referenceType = Persistence.getInstance().getPersistedClass(getType());
	}
	
	@Override
	public String getColumnName()
	{
		String idName = referenceType.getIdField().getColumnName();
		idName = idName.substring(0, 1).toUpperCase() + idName.substring(1);
		return name + idName;
	}
	
	@Override
	public DataType getColumnType()
	{
		Class<?> referenceIdType = referenceType.getIdField().getType();
		return DataType.getTypeFromClass(referenceIdType);
	}
	
	private PersistedClass referenceType = null;

}
