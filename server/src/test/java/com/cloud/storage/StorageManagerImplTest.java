// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.cloudstack.framework.config.ConfigDepot;
import org.apache.cloudstack.framework.config.dao.ConfigurationDao;
import org.apache.cloudstack.storage.datastore.db.PrimaryDataStoreDao;
import org.apache.cloudstack.storage.datastore.db.StoragePoolVO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.cloud.agent.api.StoragePoolInfo;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.host.Host;
import com.cloud.storage.dao.VolumeDao;
import com.cloud.vm.VMInstanceVO;
import com.cloud.vm.dao.VMInstanceDao;

@RunWith(MockitoJUnitRunner.class)
public class StorageManagerImplTest {

    @Mock
    VolumeDao _volumeDao;

    @Mock
    VMInstanceDao vmInstanceDao;
    @Mock
    ConfigDepot configDepot;
    @Mock
    ConfigurationDao configurationDao;
    @Mock
    DataCenterDao dataCenterDao;

    @Spy
    @InjectMocks
    private StorageManagerImpl storageManagerImpl;

    @Test
    public void createLocalStoragePoolName() {
        String hostMockName = "host1";
        executeCreateLocalStoragePoolNameForHostName(hostMockName);
    }

    @Test
    public void createLocalStoragePoolNameUsingHostNameWithSpaces() {
        String hostMockName = "      hostNameWithSpaces      ";
        executeCreateLocalStoragePoolNameForHostName(hostMockName);
    }

    private void executeCreateLocalStoragePoolNameForHostName(String hostMockName) {
        String firstBlockUuid = "dsdsh665";

        String expectedLocalStorageName = hostMockName.trim() + "-local-" + firstBlockUuid;

        Host hostMock = Mockito.mock(Host.class);
        StoragePoolInfo storagePoolInfoMock = Mockito.mock(StoragePoolInfo.class);

        Mockito.when(hostMock.getName()).thenReturn(hostMockName);
        Mockito.when(storagePoolInfoMock.getUuid()).thenReturn(firstBlockUuid + "-213151-df21ef333d-2d33f1");

        String localStoragePoolName = storageManagerImpl.createLocalStoragePoolName(hostMock, storagePoolInfoMock);
        Assert.assertEquals(expectedLocalStorageName, localStoragePoolName);
    }

    private VolumeVO mockVolumeForIsVolumeSuspectedDestroyDuplicateTest() {
        VolumeVO volumeVO = new VolumeVO("data", 1L, 1L, 1L, 1L, 1L, "data", "data", Storage.ProvisioningType.THIN, 1, null, null, "data", Volume.Type.DATADISK);
        volumeVO.setPoolId(1L);
        return volumeVO;
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateNoPool() {
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        volume.setPoolId(null);
        Assert.assertFalse(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateNoPath() {
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        Assert.assertFalse(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateNoVmId() {
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        volume.setInstanceId(null);
        Assert.assertFalse(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateNoVm() {
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        Assert.assertFalse(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateNoVmVolumes() {
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        Mockito.when(vmInstanceDao.findById(1L)).thenReturn(Mockito.mock(VMInstanceVO.class));
        Mockito.when(_volumeDao.findUsableVolumesForInstance(1L)).thenReturn(new ArrayList<>());
        Assert.assertFalse(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void testIsVolumeSuspectedDestroyDuplicateTrue() {
        Long poolId = 1L;
        String path = "data";
        VolumeVO volume = mockVolumeForIsVolumeSuspectedDestroyDuplicateTest();
        volume.setPoolId(poolId);
        Mockito.when(vmInstanceDao.findById(1L)).thenReturn(Mockito.mock(VMInstanceVO.class));
        VolumeVO volumeVO = Mockito.mock(VolumeVO.class);
        Mockito.when(volumeVO.getPoolId()).thenReturn(poolId);
        Mockito.when(volumeVO.getPath()).thenReturn(path);
        Mockito.when(_volumeDao.findUsableVolumesForInstance(1L)).thenReturn(List.of(volumeVO, Mockito.mock(VolumeVO.class)));
        Assert.assertTrue(storageManagerImpl.isVolumeSuspectedDestroyDuplicateOfVmVolume(volume));
    }

    @Test
    public void storagePoolCompatibleWithVolumePoolTestVolumeWithPoolIdInAllocatedState() {
        StoragePoolVO storagePool = new StoragePoolVO();
        storagePool.setPoolType(Storage.StoragePoolType.PowerFlex);
        storagePool.setId(1L);
        VolumeVO volume = new VolumeVO();
        volume.setState(Volume.State.Allocated);
        volume.setPoolId(1L);
        PrimaryDataStoreDao storagePoolDao = Mockito.mock(PrimaryDataStoreDao.class);
        storageManagerImpl._storagePoolDao = storagePoolDao;
        Mockito.doReturn(storagePool).when(storagePoolDao).findById(volume.getPoolId());
        Assert.assertFalse(storageManagerImpl.storagePoolCompatibleWithVolumePool(storagePool, volume));

    }

    @Test
    public void storagePoolCompatibleWithVolumePoolTestVolumeWithoutPoolIdInAllocatedState() {
        StoragePoolVO storagePool = new StoragePoolVO();
        storagePool.setPoolType(Storage.StoragePoolType.PowerFlex);
        storagePool.setId(1L);
        VolumeVO volume = new VolumeVO();
        volume.setState(Volume.State.Allocated);
        PrimaryDataStoreDao storagePoolDao = Mockito.mock(PrimaryDataStoreDao.class);
        storageManagerImpl._storagePoolDao = storagePoolDao;
        Assert.assertTrue(storageManagerImpl.storagePoolCompatibleWithVolumePool(storagePool, volume));
    }

    @Test
    public void testEnableDefaultDatastoreDownloadRedirectionForExistingInstallationsNoChange() {
        Mockito.when(configDepot.isNewConfig(StorageManager.DataStoreDownloadFollowRedirects))
                .thenReturn(false);
        storageManagerImpl.enableDefaultDatastoreDownloadRedirectionForExistingInstallations();
        Mockito.verify(configurationDao, Mockito.never()).update(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testEnableDefaultDatastoreDownloadRedirectionForExistingInstallationsOldInstall() {
        Mockito.when(configDepot.isNewConfig(StorageManager.DataStoreDownloadFollowRedirects))
                .thenReturn(true);
        Mockito.when(dataCenterDao.listAll(Mockito.any()))
                .thenReturn(List.of(Mockito.mock(DataCenterVO.class)));
        Mockito.doReturn(true).when(configurationDao).update(Mockito.anyString(), Mockito.anyString());
        storageManagerImpl.enableDefaultDatastoreDownloadRedirectionForExistingInstallations();
        Mockito.verify(configurationDao, Mockito.times(1))
                .update(StorageManager.DataStoreDownloadFollowRedirects.key(), "true");
    }

    @Test
    public void testEnableDefaultDatastoreDownloadRedirectionForExistingInstallationsNewInstall() {
        Mockito.when(configDepot.isNewConfig(StorageManager.DataStoreDownloadFollowRedirects))
                .thenReturn(true);
        Mockito.when(dataCenterDao.listAll(Mockito.any()))
                .thenReturn(new ArrayList<>()); //new installation
        storageManagerImpl.enableDefaultDatastoreDownloadRedirectionForExistingInstallations();
        Mockito.verify(configurationDao, Mockito.never())
                .update(StorageManager.DataStoreDownloadFollowRedirects.key(),
                        StorageManager.DataStoreDownloadFollowRedirects.defaultValue());
    }

}
