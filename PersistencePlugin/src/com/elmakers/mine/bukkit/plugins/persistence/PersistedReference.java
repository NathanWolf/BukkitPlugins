package com.elmakers.mine.bukkit.plugins.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

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
		if (referenceType == null) return null;
		
		String idName = referenceType.getIdField().getColumnName();
		idName = name + idName.substring(0, 1).toUpperCase() + idName.substring(1);
		return idName;
	}

	@Override
	public String[] getColumnNames()
	{
		if (referenceType == null) return null;
		// TODO : support contained objects
		
		return new String[] { getColumnName() };
	}
	
	@Override
	public DataType getColumnType()
	{	
		if (referenceType == null) return null;
		
		return DataType.getTypeFromClass(referenceType.getIdField().getType());
	}
		
	@Override
	public DataType[] getColumnTypes()
	{
		if (referenceType == null) return null;
		// TOOD : support contained objects
		
		return new DataType[] { getColumnType() };
	}
	
	@Override
	public Object getColumnValue(Object o)
	{
		if (referenceType == null) return null;
		if (o == null) return null;
		
		// TODO : support contained objects
		
		Object referenceObject = get(o);
		if (referenceObject == null) return null;
		
		return referenceType.getId(referenceObject);
	}
	
	@Override
	public void setColumnValue(Object o, Object data)
	{
		if (referenceType == null) return;	
		
		deferredReferences.add(new DeferredReference(this, o, data));
	}
	
	public static void beginDefer()
	{
		deferStackDepth++;
	}
	
	public static void endDefer()
	{
		deferStackDepth--;
		if (deferStackDepth > 0) return;
		
		for (DeferredReference ref : deferredReferences)
		{
			Object reference = ref.referenceField.referenceType.get(ref.referenceId);
			ref.referenceField.set(ref.object, reference);
		}
		deferredReferences.clear();
	}
	
	class DeferredReference
	{
		public PersistedReference referenceField;
		public Object object;
		public Object referenceId;
		
		public DeferredReference(PersistedReference field, Object o, Object id)
		{
			referenceField = field;
			object = o;
			referenceId = id;
		}
	}
	
	private static int deferStackDepth = 0;
	private final static List<DeferredReference> deferredReferences = new ArrayList<DeferredReference>();

	protected PersistedClass referenceType = null;
}
