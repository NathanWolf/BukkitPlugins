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
		idName = idName.substring(0, 1).toUpperCase() + idName.substring(1);
		return name + idName;
	}
	
	@Override
	public DataType getColumnType()
	{
		if (referenceType == null) return DataType.NULL;
		
		Class<?> referenceIdType = referenceType.getIdField().getType();
		return DataType.getTypeFromClass(referenceIdType);
	}
	
	@Override
	public Object getColumnData(Object o)
	{
		if (referenceType == null) return null;
		if (o == null) return null;
		
		Object referenceObject = get(o);
		if (referenceObject == null) return null;
		
		return referenceType.getId(referenceObject);
	}
	
	@Override
	public void setColumnData(Object o, Object deferredId)
	{
		if (referenceType == null) return;	
		if (deferredId == null) 
		{
			set(o, null);
			return;
		}
		
		deferredReferences.add(new DeferredReference(this, o, deferredId));
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
	private PersistedClass referenceType = null;
	private final static List<DeferredReference> deferredReferences = new ArrayList<DeferredReference>();

}
