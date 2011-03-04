package com.elmakers.mine.craftbukkit.persistence.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import com.elmakers.mine.bukkit.persistence.FieldInfo;
import com.elmakers.mine.bukkit.persistence.exception.InvalidDataException;
import com.elmakers.mine.bukkit.persistence.exception.InvalidPersistedClassException;
import com.elmakers.mine.craftbukkit.persistence.Persistence;
import com.elmakers.mine.craftbukkit.persistence.data.DataField;
import com.elmakers.mine.craftbukkit.persistence.data.DataRow;
import com.elmakers.mine.craftbukkit.persistence.data.DataTable;
import com.elmakers.mine.craftbukkit.persistence.data.DataType;

public class PersistedObject extends PersistedField implements PersistedReference
{
	public PersistedObject(PersistedObject copy)
	{
		super(copy);
		
		if (isContained())
		{
			try
			{
				referenceType = new PersistedClass(copy.referenceType, this);
			}
			catch (InvalidPersistedClassException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			referenceType = copy.referenceType;
		}
	}

	public PersistedObject clone()
	{
		PersistedObject field = new PersistedObject(this);
		return field;
	}

	public PersistedObject(FieldInfo fieldInfo, Field field, PersistedClass owningClass)
	{
		super(fieldInfo, field, owningClass);
	}
	
	public PersistedObject(FieldInfo fieldInfo, Method getter, Method setter, PersistedClass owningClass)
	{
		super(fieldInfo, getter, setter, owningClass);
	}
	
	@Override
	public void bind() throws InvalidPersistedClassException
	{
		try
		{
			referenceType = Persistence.getInstance().getPersistedClass(getType());
		}
		catch (InvalidPersistedClassException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (referenceType == null)
		{			
			log.severe("Persistence: Reference field: " + getDataName() + " has no valid reference type");
			return;
		}
		
		if (isContained() || referenceType.hasContainer())
		{
			// Create a sub-class of the reference class
			referenceType = new PersistedClass(referenceType, this);
			referenceType.bindReferences();
		}
		else
		{
			if (referenceType.isContainedClass())
			{
				log.warning("Persistence: " + owningClass.getSchemaName() + "." + owningClass.getTableName() + "." + getDataName() + ", entity " + referenceType.getTableName() + " must be contained");
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
		
		return getConcreteField().getDataType();
	}
	
	public void populateHeader(DataTable dataTable)
	{
		if (isContained() && referenceType != null)
		{
			referenceType.populateHeader(dataTable);
			return;
		}
		
		DataRow headerRow = dataTable.getHeader();
		DataField field = new DataField(getDataName(), getDataType());
		field.setIdField(isIdField());
		if (headerRow != null)
		{
			headerRow.add(field);
		}
	}

	public void save(DataRow row, Object o) throws InvalidDataException
	{
		if (referenceType == null) return;	
		
		if (referenceType.hasContainer())
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
				referenceId = referenceType.getIdData(reference);
			}
		}
		
		DataField field = new DataField(getDataName(), getDataType(), referenceId);
		row.add(field);
	}
	
	public void load(DataRow row, Object o) throws InvalidDataException
	{
		if (referenceType == null) return;	
		
		if (isContained())
		{
			Object newInstance = null;
			try
			{
				referenceType.createInstance(row);
			}
			catch (InvalidDataException e)
			{
			}
			set(o, newInstance);
			return;
		}
		
		DataField dataField = row.get(getDataName());
		Object referenceId = null;
		if (dataField != null)
		{
			referenceId = dataField.getValue();
		}
		
		if (referenceId == null)
		{
			set(o, referenceId);
		}
		else
		{
			deferredReferences.add(new DeferredReference(this, o, referenceId));
		}
	}
		
	public static void beginDefer()
	{
		deferStackDepth++;
	}
	
	public static void endDefer()
	{
		deferStackDepth--;
		if (deferStackDepth > 0) return;
		
		List<DeferredReference> undefer = new ArrayList<DeferredReference>();
		undefer.addAll(deferredReferences);
		deferredReferences.clear();
		
		for (DeferredReference ref : undefer)
		{
			Object reference = ref.referenceField.referenceType.get(ref.referenceId);
			try
			{
				ref.referenceField.set(ref.object, reference);
			}
			catch (InvalidDataException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Re-add to cache so that we can cache by the new id
			// Unless this is a contained object, in which case it has no id!
			if (!ref.referenceField.hasContainer())
			{
				ref.referenceField.owningClass.addToCache(ref.object);
			}
		}
		
	}
	
	class DeferredReference
	{
		public PersistedObject referenceField;
		public Object object;
		public Object referenceId;
		
		public DeferredReference(PersistedObject field, Object o, Object id)
		{
			referenceField = field;
			object = o;
			referenceId = id;
		}
	}
	
	// Persisted Reference interface
	
	public boolean isObject()
	{
		return true;
	}
	
	public PersistedClass getReferenceType()
	{
		return referenceType;
	}
	
	private static int deferStackDepth = 0;
	private final static List<DeferredReference> deferredReferences = new ArrayList<DeferredReference>();

	protected PersistedClass referenceType = null;
}
