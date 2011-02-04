package com.elmakers.mine.bukkit.plugins.persistence.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import com.elmakers.mine.bukkit.plugins.persistence.Persistence;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataField;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataRow;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataTable;
import com.elmakers.mine.bukkit.plugins.persistence.data.DataType;

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
		if (contained)
		{
			// Create a sub-class of the reference class
			referenceType = new PersistedClass(referenceType);
		}
	}
	
	@Override
	public String getDataName()
	{
		if (referenceType == null) return null;
		
		String idName = referenceType.getIdField().getDataName();
		idName = name + idName.substring(0, 1).toUpperCase() + idName.substring(1);
		return idName;
	}

	@Override
	public DataType getDataType()
	{	
		if (referenceType == null) return null;
		
		return DataType.getTypeFromClass(referenceType.getIdField().getType());
	}
	
	public void populateHeader(DataTable dataTable)
	{
		if (contained)
		{
			referenceType.populateHeader(dataTable);
			return;
		}
		
		DataRow headerRow = dataTable.getHeader();
		DataField field = new DataField(getName(), getDataType());
		if (headerRow != null)
		{
			headerRow.add(field);
		}
	}

	public void save(DataRow row, Object o)
	{
		if (referenceType == null) return;	
		
		if (contained)
		{
			referenceType.populate(row, o);
			return;
		}
		
		Object referenceId = null;
		if (o != null)
		{
			Object reference = get(o);
			if (reference != null)
			{
				referenceId = referenceType.getId(reference);
			}
		}
		
		DataField field = new DataField(getName(), getDataType(), referenceId);
		row.add(field);
	}
	
	public void load(DataRow row, Object o)
	{
		if (referenceType == null) return;	
		
		if (contained)
		{
			referenceType.load(row, o);
			return;
		}
		
		DataField dataField = row.get(getName());
		Object referenceId = dataField.getValue();
		
		deferredReferences.add(new DeferredReference(this, o, referenceId));
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
