package org.osgi.service.onem2m.impl.protocol.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.service.onem2m.ServiceLayer;
import org.osgi.service.onem2m.dto.AttributeDTO;
import org.osgi.service.onem2m.dto.FilterCriteriaDTO;
import org.osgi.service.onem2m.dto.NotificationDTO;
import org.osgi.service.onem2m.dto.PrimitiveContentDTO;
import org.osgi.service.onem2m.dto.RequestPrimitiveDTO;
import org.osgi.service.onem2m.dto.RequestPrimitiveDTO.DiscoveryResultType;
import org.osgi.service.onem2m.dto.RequestPrimitiveDTO.Operation;
import org.osgi.service.onem2m.dto.ResourceDTO;
import org.osgi.service.onem2m.dto.ResponsePrimitiveDTO;
import org.osgi.service.onem2m.impl.serialization.LongShortConverter;
import org.osgi.test.cse.toyCse.CseService;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLayreImplService implements ServiceLayer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLayreImplService.class);
	private CseService cse;
	private final String origin;

	public ServiceLayreImplService(String origin, CseService cse){
		this.cse = cse;
		this.origin = origin;
	}

	@Override
	public Promise<ResponsePrimitiveDTO> request(RequestPrimitiveDTO request) {

		Deferred<ResponsePrimitiveDTO> dret = new Deferred<ResponsePrimitiveDTO>();
		class Exec implements Runnable{
			Deferred<ResponsePrimitiveDTO> dret;
			Exec(Deferred<ResponsePrimitiveDTO> dret){
				this.dret = dret;
			}
			@Override
			public void run() {
				ResponsePrimitiveDTO ret = new ResponsePrimitiveDTO();
				switch(request.operation){
					case Create:
						ret = cse.create(request);
						break;

					case Retrieve:
						ret = cse.retrieve(request);
						break;

					case Update:
						ret = cse.update(request);
						break;

					case Delete:
						ret = cse.delete(request);
						break;

					case Notify:

						break;
				}
				dret.resolve(ret);
			}
		}

		Exec ee = new Exec(dret);
		Thread t = new Thread(ee);
		t.start();

		return dret.getPromise();
	}

	@Override
	public Promise<ResourceDTO> create(String uri, ResourceDTO resource) {
		LOGGER.info("START CREATE");
		LOGGER.debug("Request Uri is [" + uri + "].");


		// When DTO is NULL End without request processing
		if (resource == null) {
			LOGGER.warn("END CREATE");
			return null;
		}

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.content.resource = resource;
		req.to = uri;
		req.operation = Operation.Create;

		// Set the source of the request
		req.from = this.origin;

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		LOGGER.info("END CREATE");
		return res.map(p -> p.content.resource);
	}

	@Override
	public Promise<ResourceDTO> retrieve(String uri) {
		LOGGER.info("START RETRIEVE");
		LOGGER.debug("Request Uri is [" + uri + "].");

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.to = uri;
		req.operation = Operation.Retrieve;

		// Set the source of the request
		req.from = this.origin;

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		// RETRIEVE processing end
		LOGGER.info("END RETRIEVE");

		return res.map(p -> p.content.resource);
	}

	@Override
	public Promise<ResourceDTO> retrieve(String uri, List<String> targetAttributes) {
		LOGGER.info("START RETRIEVE");
		LOGGER.debug("Request Uri is [" + uri + "].");

		// When List is NULL or size 0, it terminates without request processing
		if (targetAttributes == null || targetAttributes.size() == 0) {
			LOGGER.warn("END RETRIEVE");
			return null;
		}

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.content.listOfURIs = targetAttributes;
		req.operation = Operation.Retrieve;

		// Set the source of the request
		req.from = this.origin;

		uri += "?atrl=";
		for(String param : targetAttributes) {
			uri += param + "+";
		}

		req.to = uri.substring(0, uri.length() - 1);

		LOGGER.debug("Request Uri is [" + req.to + "].");

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		// RETRIEVE processing end
		LOGGER.info("END RETRIEVE");

		return res.map(p -> p.content.resource);
	}

	@Override
	public Promise<ResourceDTO> update(String uri, ResourceDTO resource) {
		LOGGER.info("START UPDATE");
		LOGGER.debug("Request Uri is [" + uri + "].");

		// When DTO is NULL End without request processing
		if (resource == null) {
			LOGGER.warn("END UPDATE");
			return null;
		}

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.content.resource = resource;
		req.to = uri;
		req.operation = Operation.Update;

		// Set the source of the request
		req.from = this.origin;

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		LOGGER.info("END UPDATE");

		return res.map(p -> p.content.resource);
	}

	@Override
	public Promise<Boolean> delete(String uri) {
		LOGGER.info("START DELETE");
		LOGGER.debug("Request Uri is [" + uri + "].");

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.to = uri;
		req.operation = Operation.Delete;

		// Set the source of the request
		req.from = this.origin;

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		LOGGER.info("END DELETE");

		return res.map(p -> {
			if (p.responseStatusCode >= 2000 && p.responseStatusCode < 3000) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		});
	}

	@Override
	public Promise<List<String>> discovery(String uri, FilterCriteriaDTO fc) {
		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.operation = Operation.Retrieve;
		req.to = uri;
		req.filterCriteria = fc;
		req.discoveryResultType = DiscoveryResultType.structured;

		// Set the source of the request
		req.from = this.origin;

		LOGGER.info("START DISCOVERY");
		LOGGER.debug("Request Uri(BEFORE) is [" + uri + "].");

		try {
			// Add parameters to URI
			req.to = discoveryFilter(req);
		} catch (Exception e) {
			LOGGER.warn("Create filter error.", e);
			return null;
		}

		LOGGER.debug("Request Uri(AFTER) is [" + req.to + "].");

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		LOGGER.info("END DISCOVERY");

		return res.map(p -> p.content.listOfURIs);
	}

	@Override
	public Promise<List<String>> discovery(String uri, FilterCriteriaDTO fc, DiscoveryResultType drt) {
		LOGGER.info("START DISCOVERY_RESULT_TYPE");

		// Setting RequestPrimitiveDTO
		RequestPrimitiveDTO req = new RequestPrimitiveDTO();
		req.content = new PrimitiveContentDTO();
		req.operation = Operation.Retrieve;
		req.to = uri;
		req.filterCriteria = fc;
		req.discoveryResultType = drt;

		// Set the source of the request
		req.from = this.origin;

		LOGGER.debug("Request Uri(BEFORE) is [" + uri + "].");

		try {
			// Add parameters to URI
			req.to = discoveryFilter(req);
		} catch (Exception e) {
			LOGGER.warn("Create filter error.", e);
			return null;
		}

		LOGGER.debug("Request Uri(AFTER) is [" + req.to + "].");

		// Execute request transmission processing
		Promise<ResponsePrimitiveDTO> res = this.request(req);

		LOGGER.info("END DISCOVERY_RESULT_TYPE");

		return res.map(p -> p.content.listOfURIs);
	}

	@Override
	public Promise<Boolean> notify(String uri, NotificationDTO notification) {
		// not implemented
		return null;
	}


	private List<String> dataListing(Object res) {
		String strRes = null;
		if(res instanceof byte[]){
			strRes = new String((byte[])res);
		} else {
			strRes = (String) res;
		}

		List<String> resArrayList = new ArrayList<String>();

		// Delete border
		strRes = strRes.substring(strRes.indexOf("[") + 1);
		strRes = strRes.substring(0, (strRes.lastIndexOf("]") - 1));

		// Delete unnecessary character string · space
		strRes = strRes.replace("\"", "").replace(" ", "");

		// Put the edited character string in the list
		String resArr[] = strRes.split(",", 0);
		resArrayList = Arrays.asList(resArr);

		LOGGER.debug("<Response Data List>");

		for (String str : resArrayList) {
			LOGGER.debug("[" + str + "]");
		}
		return resArrayList;
	}


	public static String discoveryFilter(RequestPrimitiveDTO req) {

		if (req.to == null) {
			LOGGER.warn("URI is NULL");
			return req.to;
		}

		String ex = req.to;

		try {
			Boolean questionFlg = false;
			Boolean filterFlg = req.filterCriteria != null ? true : false;
			Boolean resultTypeFlg = req.discoveryResultType != null ? true : false;

			if (filterFlg) {
				//  Get field of filterCriteria
				FilterCriteriaDTO fc = req.filterCriteria;
				Field[] field = fc.getClass().getFields();

				// Process only the number of items in the field
				for (Field s : field) {
					if (s.get(fc) != null) {
						if (!questionFlg) {
							ex += "?";
							questionFlg = true;
						}
						// Whether the type is List
						if (s.getType().equals(List.class)) {
							Type t = s.getGenericType();
							if (t instanceof ParameterizedType) {
								ParameterizedType paramType = (ParameterizedType) t;
								Type[] argTypes = paramType.getActualTypeArguments();
								if (argTypes.length > 0) {
									Type at = argTypes[0];
									if (at.equals(String.class)) {
										for (String str : (List<String>) s.get(fc)) {
											ex += LongShortConverter.l2s(s.getName()) + "=" + str + "&";
										}
										continue;
									} else if (at.equals(Integer.class)) {
										for (Integer strInt : (List<Integer>) s.get(fc)) {
											ex += LongShortConverter.l2s(s.getName()) + "=" + strInt.toString() + "&";
										}
										continue;
									} else if (at.equals(AttributeDTO.class)) {
										for (AttributeDTO ad : (List<AttributeDTO>) s.get(fc)) {
											ex += LongShortConverter.l2s(s.getName()) + "=" + ad.name + "&";
										}
										continue;
									}
								}
							}
						}
						// Whether it is filterOperation
						else if ("filterOperation".equals(s.getName())) {
							ex += LongShortConverter.l2s(s.getName()) + "=" + fc.filterOperation.getValue();
						}
						// Whether it is filterUsage
						else if ("filterUsage".equals(s.getName())) {
							ex += LongShortConverter.l2s(s.getName()) + "=" + fc.filterUsage.getValue();
						}
						else {
							for (int i = 0; field.length > i; i++) {
								if (field[i].getName().equals(s.getName())) {
									ex += LongShortConverter.l2s(s.getName()) + "=" + s.get(fc);
									break;
								}
								else if (field.length == (i + 1)) {
									LOGGER.warn("This Column is NOT COVERED to \"ChangeName => \" " + s.getName());
								}
							}
						}
					} else {
						// If the value is NULL, go to the next item
						continue;
					}
					ex += "&";
				}
			}

			// Setting the ResultType and the URI that added the query more than once
			if (resultTypeFlg && questionFlg) {
				ex += "drt=" + req.discoveryResultType.getValue();
			}
			// Setting of ResultType setting
			else if (resultTypeFlg) {
				ex += "?drt=" + req.discoveryResultType.getValue();
			}
			// URI with no ResultType setting and one or more queries added
			else if (questionFlg) {
				ex = ex.substring(0, ex.length() - 1);
			}
		} catch (Exception e) {
			LOGGER.warn("Create filter error.", e);
		}
		return ex;
	}

}