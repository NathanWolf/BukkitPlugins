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
			referenceType = new PersistedClass(referenceType, this);
			referenceType.bindReferences();
		}
		else
		{
			if (referenceType.contained)
			{
				log.warning("Persistence: Field: " + getDataName() + ", Class " + referenceType.getTableName() + " must be contained");
				referenceType = null;
			}
		}
	}
	
	@Override
	public String getDataName()
	{
		if (referenceType == null) return null;
		
		if (container != null)
		{
			return getContainedName(container.getDataName(), name);
		}

		PersistedField idField = referenceType.getIdField();
		String dataName = name;
		if (idField != null)
		{
			String idName = idField.getDataName();
			String idRemainder = "";
			if (idName.length() > 1)
			{
				idRemainder = idName.substring(1);
			}
			dataName += idName.substring(0, 1).toUpperCase() + idRemainder;
		}
		return dataName;
	}

	@Override
	public DataType getDataType()
	{	
		if (referenceType == null) return null;
		
		return DataType.getTypeFromClass(referenceType.getIdField().getType());
	}
	
	public void populateHeader(DataTable dataTable)
	{
		if (contained && referenceType != null)
		{
			referenceType.populateHeader(dataTable);
			return;
		}
		
		DataRow headerRow = dataTable.getHeader();
		DataField field = new DataField(getDataName(), getDataType());
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
			Object containedData = get(o);
			referenceType.populate(row, containedData);
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
		
		DataField field = new DataField(getDataName(), getDataType(), referenceId);
		row.add(field);
	}
	
	public void load(DataRow row, Object o)
	{
		if (referenceType == null) return;	
		
		if (contained)
		{
			Object newInstance = referenceType.createInstance(row);
			set(o, newInstance);
			return;
		}
		
		DataField dataField = row.get(getDataName());
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
