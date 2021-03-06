/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.repository.query;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.data.cassandra.mapping.CassandraSimpleTypeHolder;
import org.springframework.data.cassandra.mapping.CassandraType;
import org.springframework.data.cassandra.repository.query.CassandraParameters.CassandraParameter;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.Parameters;

import com.datastax.driver.core.DataType;

/**
 * Custom extension of {@link Parameters} discovering additional properties of query method parameters.
 *
 * @author Matthew Adams
 * @author Mark Paluch
 */
public class CassandraParameters extends Parameters<CassandraParameters, CassandraParameter> {

	/**
	 * Creates a new {@link CassandraParameters} instance from the given {@link Method}
	 *
	 * @param method must not be {@literal null}.
	 */
	public CassandraParameters(Method method) {
		super(method);
	}

	private CassandraParameters(List<CassandraParameter> originals) {
		super(originals);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.query.Parameters#createParameter(org.springframework.core.MethodParameter)
	 */
	@Override
	protected CassandraParameter createParameter(MethodParameter parameter) {
		return new CassandraParameter(parameter);
	}

	/* (non-Javadoc)
	 * @see org.springframework.data.repository.query.Parameters#createFrom(java.util.List)
	 */
	@Override
	protected CassandraParameters createFrom(List<CassandraParameter> parameters) {
		return new CassandraParameters(parameters);
	}

	/**
	 * Custom {@link Parameter} implementation adding {@link CassandraType} support.
	 *
	 * @author Mark Paluch
	 */
	class CassandraParameter extends Parameter {

		private final DataType dataType;

		protected CassandraParameter(MethodParameter parameter) {

			super(parameter);

			if (parameter.hasParameterAnnotation(CassandraType.class)) {

				CassandraType cassandraType = parameter.getParameterAnnotation(CassandraType.class);

				if (cassandraType.type() == null) {
					throw new IllegalArgumentException(
							String.format("You must specify the type() when annotating method parameters with @%s",
									CassandraType.class.getSimpleName()));
				}

				this.dataType = CassandraSimpleTypeHolder.getDataTypeFor(cassandraType.type());
			} else {
				this.dataType = CassandraSimpleTypeHolder.getDataTypeFor(getType());
			}
		}

		/**
		 * Returns the Cassandra {@link DataType} for the declared parameter if the type is a
		 * {@link org.springframework.data.cassandra.mapping.CassandraSimpleTypeHolder simple type}. Parameter types may be
		 * specified using {@link org.springframework.data.cassandra.mapping.CassandraType}.
		 * 
		 * @return the Cassandra {@link DataType} or {@literal null} if the parameter type cannot be determined from
		 *         {@link org.springframework.data.cassandra.mapping.CassandraSimpleTypeHolder}
		 */
		public DataType getCassandraType() {
			return dataType;
		}
	}
}
