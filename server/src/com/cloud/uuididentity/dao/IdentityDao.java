// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// Automatically generated by addcopyright.py at 04/03/2012
package com.cloud.uuididentity.dao;

import com.cloud.api.IdentityMapper;
import com.cloud.server.ResourceTag.TaggedResourceType;
import com.cloud.utils.Pair;
import com.cloud.utils.db.GenericDao;

public interface IdentityDao extends GenericDao<IdentityVO, Long> {
	Long getIdentityId(IdentityMapper mapper, String identityString);
    Long getIdentityId(String tableName, String identityString);
	String getIdentityUuid(String tableName, String identityString);
	void initializeDefaultUuid(String tableName);
    /**
     * @param tableName
     * @param identityId
     * @param resourceType TODO
     * @return
     */
    Pair<Long, Long> getAccountDomainInfo(String tableName, Long identityId, TaggedResourceType resourceType);
}
