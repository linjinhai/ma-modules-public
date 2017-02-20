/**
 * Copyright (C) 2016 Infinite Automation Software. All rights reserved.
 *
 */
package com.infiniteautomation.mango.rest.v2.util;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import com.infiniteautomation.mango.web.mvc.rest.v2.exception.NotFoundRestException;
import com.serotonin.m2m2.Common;

/**
 * Container to Manage Temporary Resources
 * 
 * ResourceIds are used via:
 * 
 * GET/PUT
 * url-prefix/resourceId
 * 
 * @author Terry Packer
 */
public class MangoRestTemporaryResourceContainer<T extends MangoRestTemporaryResource> {

	//Map for recently run imports
	private final ConcurrentHashMap<String,T> resources;
	private final String resourcePrefix;
	
	public MangoRestTemporaryResourceContainer(String prefix){
		this.resources = new ConcurrentHashMap<String,T>();
		this.resourcePrefix = prefix;
	}
	
	/**
	 * Get a resource if exists else throw exception
	 * @param id
	 * @return
	 * @throws NotFoundRestException
	 */
	public T get(String id) throws NotFoundRestException{
		T resource = this.resources.get(id);
		if(resource == null)
			throw new NotFoundRestException();
		else
			return resource;
	}
	
	/**
	 * Add a Resource, with a timeout of not null
	 * @param id
	 * @param resource
	 */
	public void put(String id, T resource, long expiration){
		this.resources.put(id, resource);
		if(expiration > 0)
			resource.schedule(expiration, this);
	}
	
	/**
	 * Remove a resource and cancel its timeout if there is one
	 * @param resourceId
	 * @return
	 */
	public boolean remove(String resourceId){
		T resource = this.resources.remove(resourceId);
		if(resource != null){
			resource.cancelTimeout();
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Generate a unique resource Id for this container
	 * @return
	 */
	public String generateResourceId(){
		String resourceId = Common.generateXid(resourcePrefix);
        while (!isResourceIdUnique(resourceId))
        	resourceId = Common.generateXid(resourcePrefix);
        return resourceId;
	}
	
	private boolean isResourceIdUnique(String resourceId){
		Iterator<String> it = this.resources.keySet().iterator();
		while(it.hasNext()){
			if(it.next().equals(resourceId))
				return false;
		}
		return true;
	}
	
	
}
