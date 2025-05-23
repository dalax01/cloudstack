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
package org.apache.cloudstack.api.response;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;

import com.cloud.domain.DomainVO;
import com.cloud.domain.dao.DomainDao;
import com.cloud.exception.PermissionDeniedException;
import com.cloud.user.AccountManager;
import com.cloud.user.UserVO;
import com.cloud.utils.Pair;
import com.cloud.utils.exception.CloudRuntimeException;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.command.QuotaConfigureEmailCmd;
import org.apache.cloudstack.api.command.QuotaCreditsListCmd;
import org.apache.cloudstack.api.command.QuotaEmailTemplateListCmd;
import org.apache.cloudstack.api.command.QuotaEmailTemplateUpdateCmd;
import org.apache.cloudstack.api.command.QuotaValidateActivationRuleCmd;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.discovery.ApiDiscoveryService;
import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.jsinterpreter.JsInterpreterHelper;
import org.apache.cloudstack.quota.QuotaService;
import org.apache.cloudstack.quota.QuotaStatement;
import org.apache.cloudstack.quota.activationrule.presetvariables.PresetVariableDefinition;
import org.apache.cloudstack.quota.activationrule.presetvariables.PresetVariables;
import org.apache.cloudstack.quota.activationrule.presetvariables.Value;
import org.apache.cloudstack.quota.constant.QuotaConfig;
import org.apache.cloudstack.quota.constant.QuotaTypes;
import org.apache.cloudstack.quota.dao.QuotaAccountDao;
import org.apache.cloudstack.quota.dao.QuotaBalanceDao;
import org.apache.cloudstack.quota.dao.QuotaCreditsDao;
import org.apache.cloudstack.quota.dao.QuotaEmailConfigurationDao;
import org.apache.cloudstack.quota.dao.QuotaEmailTemplatesDao;
import org.apache.cloudstack.quota.dao.QuotaTariffDao;
import org.apache.cloudstack.quota.dao.QuotaUsageDao;
import org.apache.cloudstack.quota.vo.QuotaAccountVO;
import org.apache.cloudstack.quota.vo.QuotaBalanceVO;
import org.apache.cloudstack.quota.vo.QuotaCreditsVO;
import org.apache.cloudstack.quota.vo.QuotaEmailConfigurationVO;
import org.apache.cloudstack.quota.vo.QuotaEmailTemplatesVO;
import org.apache.cloudstack.quota.vo.QuotaTariffVO;
import org.apache.cloudstack.utils.jsinterpreter.JsInterpreter;

import org.apache.commons.lang3.time.DateUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.cloud.exception.InvalidParameterValueException;
import com.cloud.user.Account;
import com.cloud.user.AccountVO;
import com.cloud.user.dao.AccountDao;
import com.cloud.user.dao.UserDao;
import com.cloud.user.User;

import junit.framework.TestCase;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QuotaResponseBuilderImplTest extends TestCase {

    @Mock
    QuotaTariffDao quotaTariffDaoMock;

    @Mock
    QuotaBalanceDao quotaBalanceDaoMock;

    @Mock
    QuotaCreditsDao quotaCreditsDaoMock;

    @Mock
    QuotaEmailTemplatesDao quotaEmailTemplateDaoMock;

    @Mock
    UserDao userDaoMock;

    @Mock
    User userMock;

    @Mock
    ApiDiscoveryService discoveryServiceMock;

    @Mock
    QuotaService quotaServiceMock;

    @Mock
    AccountDao accountDaoMock;

    @Mock
    Consumer<String> consumerStringMock;

    @Mock
    QuotaTariffVO quotaTariffVoMock;

    @Mock
    QuotaStatement quotaStatementMock;

    @Mock
    DomainDao domainDaoMock;

    @Mock
    QuotaUsageDao quotaUsageDaoMock;

    @Mock
    QuotaAccountDao quotaAccountDaoMock;

    @Mock
    QuotaEmailConfigurationDao quotaEmailConfigurationDaoMock;

    @InjectMocks
    @Spy
    QuotaResponseBuilderImpl quotaResponseBuilderSpy;

    Date date = new Date();

    @Mock
    Account accountMock;

    @Mock
    DomainVO domainVOMock;

    @Mock
    QuotaConfigureEmailCmd quotaConfigureEmailCmdMock;

    @Mock
    QuotaAccountVO quotaAccountVOMock;

    @Mock
    QuotaEmailTemplatesVO quotaEmailTemplatesVoMock;

    @Mock
    QuotaCreditsVO quotaCreditsVoMock;

    @Mock
    UserVO userVoMock;

    @Mock
    AccountManager accountManagerMock;

    @Mock
    Account callerAccountMock;

    @Mock
    User callerUserMock;

    @Before
    public void setup() {
        CallContext.register(callerUserMock, callerAccountMock);
    }

    private void overrideDefaultQuotaEnabledConfigValue(final Object value) throws IllegalAccessException, NoSuchFieldException {
        Field f = ConfigKey.class.getDeclaredField("_defaultValue");
        f.setAccessible(true);
        f.set(QuotaConfig.QuotaAccountEnabled, value);
    }

    private Calendar[] createPeriodForQuotaSummary() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        return new Calendar[] {calendar, calendar};
    }

    @Mock
    QuotaValidateActivationRuleCmd quotaValidateActivationRuleCmdMock = Mockito.mock(QuotaValidateActivationRuleCmd.class);

    @Mock
    JsInterpreterHelper jsInterpreterHelperMock = Mockito.mock(JsInterpreterHelper.class);

    private QuotaTariffVO makeTariffTestData() {
        QuotaTariffVO tariffVO = new QuotaTariffVO();
        tariffVO.setUsageType(QuotaTypes.IP_ADDRESS);
        tariffVO.setUsageName("ip address");
        tariffVO.setUsageUnit("IP-Month");
        tariffVO.setCurrencyValue(BigDecimal.valueOf(100.19));
        tariffVO.setEffectiveOn(new Date());
        tariffVO.setUsageDiscriminator("");
        return tariffVO;
    }

    @Test
    public void testQuotaResponse() {
        QuotaTariffVO tariffVO = makeTariffTestData();
        QuotaTariffResponse response = quotaResponseBuilderSpy.createQuotaTariffResponse(tariffVO, true);
        assertTrue(tariffVO.getUsageType() == response.getUsageType());
        assertTrue(tariffVO.getCurrencyValue().equals(response.getTariffValue()));
    }

    @Test
    public void createQuotaTariffResponseTestIfReturnsActivationRuleWithPermission() {
        QuotaTariffVO tariff = makeTariffTestData();
        tariff.setActivationRule("x === 10");

        QuotaTariffResponse tariffResponse = quotaResponseBuilderSpy.createQuotaTariffResponse(tariff, true);
        assertEquals("x === 10", tariffResponse.getActivationRule());
    }

    @Test
    public void createQuotaTariffResponseTestIfReturnsActivationRuleWithoutPermission() {
        QuotaTariffVO tariff = makeTariffTestData();
        tariff.setActivationRule("x === 10");

        QuotaTariffResponse tariffResponse = quotaResponseBuilderSpy.createQuotaTariffResponse(tariff, false);
        assertNull(tariffResponse.getActivationRule());
    }

    @Test
    public void testAddQuotaCredits() {
        final long accountId = 2L;
        final long domainId = 1L;
        final double amount = 11.0;
        final long updatedBy = 2L;

        QuotaCreditsVO credit = new QuotaCreditsVO();
        credit.setCredit(new BigDecimal(amount));

        Mockito.when(quotaCreditsDaoMock.saveCredits(Mockito.any(QuotaCreditsVO.class))).thenReturn(credit);
        Mockito.when(quotaBalanceDaoMock.lastQuotaBalance(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(Date.class))).thenReturn(new BigDecimal(111));
        Mockito.doReturn(userVoMock).when(quotaResponseBuilderSpy).getCreditorForQuotaCredits(credit);

        AccountVO account = new AccountVO();
        account.setState(Account.State.LOCKED);
        Mockito.when(accountDaoMock.findById(Mockito.anyLong())).thenReturn(account);

        QuotaCreditsResponse resp = quotaResponseBuilderSpy.addQuotaCredits(accountId, domainId, amount, updatedBy, true);
        assertTrue(resp.getCredit().compareTo(credit.getCredit()) == 0);
    }

    @Test
    public void testListQuotaEmailTemplates() {
        QuotaEmailTemplateListCmd cmd = new QuotaEmailTemplateListCmd();
        cmd.setTemplateName("some name");
        List<QuotaEmailTemplatesVO> templates = new ArrayList<>();
        QuotaEmailTemplatesVO template = new QuotaEmailTemplatesVO();
        template.setTemplateName("template");
        templates.add(template);
        Mockito.when(quotaEmailTemplateDaoMock.listAllQuotaEmailTemplates(Mockito.anyString())).thenReturn(templates);

        Assert.assertEquals(1, quotaResponseBuilderSpy.listQuotaEmailTemplates(cmd).size());
    }

    @Test
    public void testUpdateQuotaEmailTemplate() {
        QuotaEmailTemplateUpdateCmd cmd = new QuotaEmailTemplateUpdateCmd();
        cmd.setTemplateBody("some body");
        cmd.setTemplateName("some name");
        cmd.setTemplateSubject("some subject");

        List<QuotaEmailTemplatesVO> templates = new ArrayList<>();

        Mockito.when(quotaEmailTemplateDaoMock.listAllQuotaEmailTemplates(Mockito.anyString())).thenReturn(templates);
        Mockito.when(quotaEmailTemplateDaoMock.updateQuotaEmailTemplate(Mockito.any(QuotaEmailTemplatesVO.class))).thenReturn(true);

        // invalid template test
        assertFalse(quotaResponseBuilderSpy.updateQuotaEmailTemplate(cmd));

        // valid template test
        QuotaEmailTemplatesVO template = new QuotaEmailTemplatesVO();
        template.setTemplateName("template");
        templates.add(template);
        assertTrue(quotaResponseBuilderSpy.updateQuotaEmailTemplate(cmd));
    }

    @Test
    public void testCreateQuotaLastBalanceResponse() {
        List<QuotaBalanceVO> quotaBalance = new ArrayList<>();
        // null balance test
        try {
            quotaResponseBuilderSpy.createQuotaLastBalanceResponse(null, new Date());
        } catch (InvalidParameterValueException e) {
            assertTrue(e.getMessage().equals("There are no balance entries on or before the requested date."));
        }

        // empty balance test
        try {
            quotaResponseBuilderSpy.createQuotaLastBalanceResponse(quotaBalance, new Date());
        } catch (InvalidParameterValueException e) {
            assertTrue(e.getMessage().equals("There are no balance entries on or before the requested date."));
        }

        // valid balance test
        QuotaBalanceVO entry = new QuotaBalanceVO();
        entry.setAccountId(2L);
        entry.setCreditBalance(new BigDecimal(100));
        quotaBalance.add(entry);
        quotaBalance.add(entry);
        QuotaBalanceResponse resp = quotaResponseBuilderSpy.createQuotaLastBalanceResponse(quotaBalance, null);
        assertTrue(resp.getStartQuota().compareTo(new BigDecimal(200)) == 0);
    }

    @Test
    public void testStartOfNextDayWithoutParameters() {
        Date nextDate = quotaResponseBuilderSpy.startOfNextDay();

        LocalDateTime tomorrowAtStartOfTheDay = LocalDate.now().atStartOfDay().plusDays(1);
        Date expectedNextDate = Date.from(tomorrowAtStartOfTheDay.atZone(ZoneId.systemDefault()).toInstant());

        Assert.assertEquals(expectedNextDate, nextDate);
    }

    @Test
    public void testStartOfNextDayWithParameter() {
        Date anyDate = new Date(1242421545757532l);

        Date nextDayDate = quotaResponseBuilderSpy.startOfNextDay(anyDate);

        LocalDateTime nextDayLocalDateTimeAtStartOfTheDay = anyDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1).atStartOfDay();
        Date expectedNextDate = Date.from(nextDayLocalDateTimeAtStartOfTheDay.atZone(ZoneId.systemDefault()).toInstant());

        Assert.assertEquals(expectedNextDate, nextDayDate);
    }

    @Test
    public void validateStringsOnCreatingNewQuotaTariffTestNullValueDoNothing() {
        quotaResponseBuilderSpy.validateStringsOnCreatingNewQuotaTariff(consumerStringMock, null);
        Mockito.verify(consumerStringMock, Mockito.never()).accept(Mockito.anyString());
    }

    @Test
    public void validateStringsOnCreatingNewQuotaTariffTestEmptyValueCallMethodWithNull() {
        quotaResponseBuilderSpy.validateStringsOnCreatingNewQuotaTariff(consumerStringMock, "");
        Mockito.verify(consumerStringMock).accept(null);
    }

    @Test
    public void validateStringsOnCreatingNewQuotaTariffTestValueCallMethodWithValue() {
        String value = "test";
        quotaResponseBuilderSpy.validateStringsOnCreatingNewQuotaTariff(consumerStringMock, value);
        Mockito.verify(consumerStringMock).accept(value);
    }

    @Test
    public void validateValueOnCreatingNewQuotaTariffTestNullValueDoNothing() {
        quotaResponseBuilderSpy.validateValueOnCreatingNewQuotaTariff(quotaTariffVoMock, null);
        Mockito.verify(quotaTariffVoMock, Mockito.never()).setCurrencyValue(Mockito.any(BigDecimal.class));
    }

    @Test
    public void validateValueOnCreatingNewQuotaTariffTestAnyValueIsSet() {
        Double value = 0.0;
        quotaResponseBuilderSpy.validateValueOnCreatingNewQuotaTariff(quotaTariffVoMock, value);
        Mockito.verify(quotaTariffVoMock).setCurrencyValue(BigDecimal.valueOf(value));
    }

    @Test
    public void validateEndDateOnCreatingNewQuotaTariffTestNullEndDateDoNothing() {
        Date startDate = null;
        Date endDate = null;

        quotaResponseBuilderSpy.validateEndDateOnCreatingNewQuotaTariff(quotaTariffVoMock, startDate, endDate);
        Mockito.verify(quotaTariffVoMock, Mockito.never()).setEndDate(Mockito.any(Date.class));
    }

    @Test (expected = InvalidParameterValueException.class)
    public void validateEndDateOnCreatingNewQuotaTariffTestEndDateLessThanStartDateThrowInvalidParameterValueException() {
        Date startDate = date;
        Date endDate = DateUtils.addSeconds(startDate, -1);

        quotaResponseBuilderSpy.validateEndDateOnCreatingNewQuotaTariff(quotaTariffVoMock, startDate, endDate);
    }

    @Test (expected = InvalidParameterValueException.class)
    public void validateEndDateOnCreatingNewQuotaTariffTestEndDateLessThanNowThrowInvalidParameterValueException() {
        Date startDate = DateUtils.addDays(date, -100);
        Date endDate = DateUtils.addDays(new Date(), -1);

        quotaResponseBuilderSpy.validateEndDateOnCreatingNewQuotaTariff(quotaTariffVoMock, startDate, endDate);
    }

    @Test
    public void validateEndDateOnCreatingNewQuotaTariffTestSetValidEndDate() {
        Date startDate = DateUtils.addDays(date, -100);
        Date endDate = DateUtils.addMinutes(new Date(), 1);

        quotaResponseBuilderSpy.validateEndDateOnCreatingNewQuotaTariff(quotaTariffVoMock, startDate, endDate);
        Mockito.verify(quotaTariffVoMock).setEndDate(Mockito.any(Date.class));
    }

    @Test
    public void getNewQuotaTariffObjectTestCreateFromCurrentQuotaTariff() throws Exception {
        try (MockedConstruction<QuotaTariffVO> quotaTariffVOMockedConstruction = Mockito.mockConstruction(QuotaTariffVO.class, (mock,
                                                                                                        context) -> {
        })) {
            QuotaTariffVO result = quotaResponseBuilderSpy.getNewQuotaTariffObject(quotaTariffVoMock, "", 0);
            Assert.assertEquals(quotaTariffVOMockedConstruction.constructed().get(0), result);
        }
    }

    @Test (expected = InvalidParameterValueException.class)
    public void getNewQuotaTariffObjectTestSetInvalidUsageTypeThrowsInvalidParameterValueException() throws InvalidParameterValueException {
        quotaResponseBuilderSpy.getNewQuotaTariffObject(null, "test", 0);
    }

    @Test
    public void getNewQuotaTariffObjectTestReturnValidObject() throws InvalidParameterValueException {
        String name = "test";
        int usageType = 1;
        QuotaTariffVO result = quotaResponseBuilderSpy.getNewQuotaTariffObject(null, name, usageType);

        Assert.assertEquals(name, result.getName());
        Assert.assertEquals(usageType, result.getUsageType());
    }

    @Test
    public void persistNewQuotaTariffTestpersistNewQuotaTariff() {
        Mockito.doReturn(quotaTariffVoMock).when(quotaResponseBuilderSpy).getNewQuotaTariffObject(Mockito.any(QuotaTariffVO.class), Mockito.anyString(), Mockito.anyInt());
        Mockito.doNothing().when(quotaResponseBuilderSpy).validateEndDateOnCreatingNewQuotaTariff(Mockito.any(QuotaTariffVO.class), Mockito.any(Date.class), Mockito.any(Date.class));
        Mockito.doNothing().when(quotaResponseBuilderSpy).validateValueOnCreatingNewQuotaTariff(Mockito.any(QuotaTariffVO.class), Mockito.anyDouble());
        Mockito.doNothing().when(quotaResponseBuilderSpy).validateStringsOnCreatingNewQuotaTariff(Mockito.any(Consumer.class), Mockito.anyString());
        Mockito.doReturn(quotaTariffVoMock).when(quotaTariffDaoMock).addQuotaTariff(Mockito.any(QuotaTariffVO.class));
        Mockito.doNothing().when(quotaResponseBuilderSpy).validatePositionOnCreatingNewQuotaTariff(Mockito.any(QuotaTariffVO.class), Mockito.anyInt());


        quotaResponseBuilderSpy.persistNewQuotaTariff(quotaTariffVoMock, "", 1, date, 1l, date, 1.0, "", "", 2);

        Mockito.verify(quotaTariffDaoMock).addQuotaTariff(Mockito.any(QuotaTariffVO.class));
    }

    @Test (expected = ServerApiException.class)
    public void deleteQuotaTariffTestQuotaDoesNotExistThrowsServerApiException() {
        quotaResponseBuilderSpy.deleteQuotaTariff("");
    }

    @Test
    public void deleteQuotaTariffTestUpdateRemoved() {
        Mockito.doReturn(quotaTariffVoMock).when(quotaTariffDaoMock).findByUuid(Mockito.anyString());
        Mockito.doReturn(true).when(quotaTariffDaoMock).updateQuotaTariff(Mockito.any(QuotaTariffVO.class));

        Assert.assertTrue(quotaResponseBuilderSpy.deleteQuotaTariff(""));

        Mockito.verify(quotaTariffVoMock).setRemoved(Mockito.any(Date.class));
    }

    @Test
    public void getQuotaSummaryResponseTestAccountIsNotNullQuotaIsDisabledShouldReturnFalse() throws NoSuchFieldException, IllegalAccessException {
        Calendar[] period = createPeriodForQuotaSummary();
        overrideDefaultQuotaEnabledConfigValue("false");

        Mockito.doReturn(period).when(quotaStatementMock).getCurrentStatementTime();
        Mockito.doReturn(domainVOMock).when(domainDaoMock).findById(Mockito.anyLong());
        Mockito.doReturn(BigDecimal.ZERO).when(quotaBalanceDaoMock).lastQuotaBalance(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(Date.class));
        Mockito.doReturn(BigDecimal.ZERO).when(quotaUsageDaoMock).findTotalQuotaUsage(Mockito.anyLong(), Mockito.anyLong(), Mockito.isNull(), Mockito.any(Date.class), Mockito.any(Date.class));

        QuotaSummaryResponse quotaSummaryResponse = quotaResponseBuilderSpy.getQuotaSummaryResponse(accountMock);

        assertFalse(quotaSummaryResponse.getQuotaEnabled());
    }

    @Test
    public void getQuotaSummaryResponseTestAccountIsNotNullQuotaIsEnabledShouldReturnTrue() throws NoSuchFieldException, IllegalAccessException {
        Calendar[] period = createPeriodForQuotaSummary();
        overrideDefaultQuotaEnabledConfigValue("true");

        Mockito.doReturn(period).when(quotaStatementMock).getCurrentStatementTime();
        Mockito.doReturn(domainVOMock).when(domainDaoMock).findById(Mockito.anyLong());
        Mockito.doReturn(BigDecimal.ZERO).when(quotaBalanceDaoMock).lastQuotaBalance(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(Date.class));
        Mockito.doReturn(BigDecimal.ZERO).when(quotaUsageDaoMock).findTotalQuotaUsage(Mockito.anyLong(), Mockito.anyLong(), Mockito.isNull(), Mockito.any(Date.class), Mockito.any(Date.class));

        QuotaSummaryResponse quotaSummaryResponse = quotaResponseBuilderSpy.getQuotaSummaryResponse(accountMock);

        assertTrue(quotaSummaryResponse.getQuotaEnabled());
    }

    @Test
    public void filterSupportedTypesTestReturnWhenQuotaTypeDoesNotMatch() throws NoSuchFieldException {
        List<Pair<String, String>> variables = new ArrayList<>();
        Class<?> clazz = Value.class;
        PresetVariableDefinition presetVariableDefinitionAnnotation = clazz.getDeclaredField("host").getAnnotation(PresetVariableDefinition.class);
        QuotaTypes quotaType = QuotaTypes.getQuotaType(QuotaTypes.NETWORK_OFFERING);
        int expectedVariablesSize = 0;

        quotaResponseBuilderSpy.filterSupportedTypes(variables, quotaType, presetVariableDefinitionAnnotation, clazz, null);

        assertEquals(expectedVariablesSize, variables.size());
    }

    @Test
    public void filterSupportedTypesTestAddPresetVariableWhenClassIsNotInstanceOfGenericPresetVariableAndComputingResource() throws NoSuchFieldException {
        List<Pair<String, String>> variables = new ArrayList<>();
        Class<?> clazz = PresetVariables.class;
        PresetVariableDefinition presetVariableDefinitionAnnotation = clazz.getDeclaredField("resourceType").getAnnotation(PresetVariableDefinition.class);
        QuotaTypes quotaType = QuotaTypes.getQuotaType(QuotaTypes.NETWORK_OFFERING);
        int expectedVariablesSize = 1;
        String expectedVariableName = "variable.name";

        quotaResponseBuilderSpy.filterSupportedTypes(variables, quotaType, presetVariableDefinitionAnnotation, clazz, "variable.name");

        assertEquals(expectedVariablesSize, variables.size());
        assertEquals(expectedVariableName, variables.get(0).first());
    }

    @Test
    public void filterSupportedTypesTestCallRecursiveMethodWhenIsGenericPresetVariableClassOrComputingResourceClass() throws NoSuchFieldException {
        List<Pair<String, String>> variables = new ArrayList<>();
        Class<?> clazz = Value.class;
        PresetVariableDefinition presetVariableDefinitionAnnotation = clazz.getDeclaredField("storage").getAnnotation(PresetVariableDefinition.class);
        QuotaTypes quotaType = QuotaTypes.getQuotaType(QuotaTypes.VOLUME);

        quotaResponseBuilderSpy.filterSupportedTypes(variables, quotaType, presetVariableDefinitionAnnotation, clazz, "variable.name");

        Mockito.verify(quotaResponseBuilderSpy, Mockito.atLeastOnce()).addAllPresetVariables(Mockito.any(), Mockito.any(QuotaTypes.class), Mockito.anyList(),
                Mockito.anyString());
    }

    @Test (expected = InvalidParameterValueException.class)
    public void validateQuotaConfigureEmailCmdParametersTestNullQuotaAccount() {
        Mockito.doReturn(null).when(quotaAccountDaoMock).findByIdQuotaAccount(Mockito.any());
        quotaResponseBuilderSpy.validateQuotaConfigureEmailCmdParameters(quotaConfigureEmailCmdMock);
    }

    @Test (expected = InvalidParameterValueException.class)
    public void validateQuotaConfigureEmailCmdParametersTestNullTemplateNameAndMinBalance() {
        Mockito.doReturn(quotaAccountVOMock).when(quotaAccountDaoMock).findByIdQuotaAccount(Mockito.any());
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getTemplateName();
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getMinBalance();
        quotaResponseBuilderSpy.validateQuotaConfigureEmailCmdParameters(quotaConfigureEmailCmdMock);
    }

    @Test (expected = InvalidParameterValueException.class)
    public void validateQuotaConfigureEmailCmdParametersTestEnableNullAndTemplateNameNotNull() {
        Mockito.doReturn(quotaAccountVOMock).when(quotaAccountDaoMock).findByIdQuotaAccount(Mockito.any());
        Mockito.doReturn(QuotaConfig.QuotaEmailTemplateTypes.QUOTA_LOW.toString()).when(quotaConfigureEmailCmdMock).getTemplateName();
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getEnable();
        quotaResponseBuilderSpy.validateQuotaConfigureEmailCmdParameters(quotaConfigureEmailCmdMock);
    }

    @Test
    public void validateQuotaConfigureEmailCmdParametersTestNullTemplateName() {
        Mockito.doReturn(quotaAccountVOMock).when(quotaAccountDaoMock).findByIdQuotaAccount(Mockito.any());
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getTemplateName();
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getEnable();
        Mockito.doReturn(100D).when(quotaConfigureEmailCmdMock).getMinBalance();
        quotaResponseBuilderSpy.validateQuotaConfigureEmailCmdParameters(quotaConfigureEmailCmdMock);
    }

    @Test
    public void validateQuotaConfigureEmailCmdParametersTestWithTemplateNameAndEnable() {
        Mockito.doReturn(quotaAccountVOMock).when(quotaAccountDaoMock).findByIdQuotaAccount(Mockito.any());
        Mockito.doReturn(QuotaConfig.QuotaEmailTemplateTypes.QUOTA_LOW.toString()).when(quotaConfigureEmailCmdMock).getTemplateName();
        Mockito.doReturn(true).when(quotaConfigureEmailCmdMock).getEnable();
        quotaResponseBuilderSpy.validateQuotaConfigureEmailCmdParameters(quotaConfigureEmailCmdMock);
    }

    @Test
    public void getQuotaEmailConfigurationVoTestTemplateNameIsNull() {
        Mockito.doReturn(null).when(quotaConfigureEmailCmdMock).getTemplateName();

        QuotaEmailConfigurationVO result = quotaResponseBuilderSpy.getQuotaEmailConfigurationVo(quotaConfigureEmailCmdMock);

        Assert.assertNull(result);
    }

    @Test (expected = InvalidParameterValueException.class)
    public void getQuotaEmailConfigurationVoTestNoTemplateFound() {
        Mockito.doReturn("name").when(quotaConfigureEmailCmdMock).getTemplateName();
        Mockito.doReturn(new ArrayList<QuotaEmailTemplatesVO>()).when(quotaEmailTemplateDaoMock).listAllQuotaEmailTemplates(Mockito.any());

        quotaResponseBuilderSpy.getQuotaEmailConfigurationVo(quotaConfigureEmailCmdMock);
    }

    @Test
    public void getQuotaEmailConfigurationVoTestNewConfiguration() {
        Mockito.doReturn("name").when(quotaConfigureEmailCmdMock).getTemplateName();
        List<QuotaEmailTemplatesVO> templatesVOArrayList = List.of(quotaEmailTemplatesVoMock);
        Mockito.doReturn(templatesVOArrayList).when(quotaEmailTemplateDaoMock).listAllQuotaEmailTemplates(Mockito.any());
        Mockito.doReturn(null).when(quotaEmailConfigurationDaoMock).findByAccountIdAndEmailTemplateId(Mockito.anyLong(), Mockito.anyLong());

        QuotaEmailConfigurationVO result = quotaResponseBuilderSpy.getQuotaEmailConfigurationVo(quotaConfigureEmailCmdMock);

        Mockito.verify(quotaEmailConfigurationDaoMock).persistQuotaEmailConfiguration(Mockito.any());
        assertEquals(0, result.getAccountId());
        assertEquals(0, result.getEmailTemplateId());
        assertFalse(result.isEnabled());
    }

    @Test
    public void getQuotaEmailConfigurationVoTestExistingConfiguration() {
        Mockito.doReturn("name").when(quotaConfigureEmailCmdMock).getTemplateName();
        List<QuotaEmailTemplatesVO> templatesVOArrayList = List.of(quotaEmailTemplatesVoMock);
        Mockito.doReturn(templatesVOArrayList).when(quotaEmailTemplateDaoMock).listAllQuotaEmailTemplates(Mockito.any());

        QuotaEmailConfigurationVO quotaEmailConfigurationVO = new QuotaEmailConfigurationVO(1, 2, true);
        Mockito.doReturn(quotaEmailConfigurationVO).when(quotaEmailConfigurationDaoMock).findByAccountIdAndEmailTemplateId(Mockito.anyLong(), Mockito.anyLong());
        Mockito.doReturn(quotaEmailConfigurationVO).when(quotaEmailConfigurationDaoMock).updateQuotaEmailConfiguration(Mockito.any());

        QuotaEmailConfigurationVO result = quotaResponseBuilderSpy.getQuotaEmailConfigurationVo(quotaConfigureEmailCmdMock);

        Mockito.verify(quotaEmailConfigurationDaoMock).updateQuotaEmailConfiguration(Mockito.any());

        assertEquals(1, result.getAccountId());
        assertEquals(2, result.getEmailTemplateId());
        assertFalse(result.isEnabled());
    }

    @Test
    public void validatePositionOnCreatingNewQuotaTariffTestNullValueDoNothing() {
        quotaResponseBuilderSpy.validatePositionOnCreatingNewQuotaTariff(quotaTariffVoMock, null);
        Mockito.verify(quotaTariffVoMock, Mockito.never()).setPosition(Mockito.any());
    }

    @Test
    public void validatePositionOnCreatingNewQuotaTariffTestAnyValueIsSet() {
        Integer position = 1;
        quotaResponseBuilderSpy.validatePositionOnCreatingNewQuotaTariff(quotaTariffVoMock, position);
        Mockito.verify(quotaTariffVoMock).setPosition(position);
    }


    @Test
    public void isUserAllowedToSeeActivationRulesTestWithPermissionToCreateTariff() {
        ApiDiscoveryResponse response = new ApiDiscoveryResponse();
        response.setName("quotaTariffCreate");

        List<ApiDiscoveryResponse> cmdList = new ArrayList<>();
        cmdList.add(response);

        ListResponse<ApiDiscoveryResponse> responseList = new ListResponse<>();
        responseList.setResponses(cmdList);

        Mockito.doReturn(responseList).when(discoveryServiceMock).listApis(userMock, null);

        assertTrue(quotaResponseBuilderSpy.isUserAllowedToSeeActivationRules(userMock));
    }

    @Test
    public void isUserAllowedToSeeActivationRulesTestWithPermissionToUpdateTariff() {
        ApiDiscoveryResponse response = new ApiDiscoveryResponse();
        response.setName("quotaTariffUpdate");

        List<ApiDiscoveryResponse> cmdList = new ArrayList<>();
        cmdList.add(response);

        ListResponse<ApiDiscoveryResponse> responseList = new ListResponse<>();
        responseList.setResponses(cmdList);

        Mockito.doReturn(responseList).when(discoveryServiceMock).listApis(userMock, null);

        assertTrue(quotaResponseBuilderSpy.isUserAllowedToSeeActivationRules(userMock));
    }

    @Test
    public void isUserAllowedToSeeActivationRulesTestWithNoPermission() {
        ApiDiscoveryResponse response = new ApiDiscoveryResponse();
        response.setName("testCmd");

        List<ApiDiscoveryResponse> cmdList = new ArrayList<>();
        cmdList.add(response);

        ListResponse<ApiDiscoveryResponse> responseList = new ListResponse<>();
        responseList.setResponses(cmdList);

        Mockito.doReturn(responseList).when(discoveryServiceMock).listApis(userMock, null);

        assertFalse(quotaResponseBuilderSpy.isUserAllowedToSeeActivationRules(userMock));
    }

    @Test
    public void createQuotaCreditsListResponseTestReturnsObject() {
        List<QuotaCreditsVO> credits = new ArrayList<>();
        credits.add(new QuotaCreditsVO());
        QuotaCreditsResponse expectedQuotaCreditsResponse = new QuotaCreditsResponse();

        Mockito.doReturn(credits).when(quotaResponseBuilderSpy).getCreditsForQuotaCreditsList(Mockito.any());
        Mockito.doReturn(userVoMock).when(quotaResponseBuilderSpy).getCreditorForQuotaCreditsList(Mockito.any(), Mockito.any());
        Mockito.doReturn(expectedQuotaCreditsResponse).when(quotaResponseBuilderSpy).createQuotaCreditsResponse(credits.get(0), userVoMock);

        Pair<List<QuotaCreditsResponse>, Integer> result = quotaResponseBuilderSpy.createQuotaCreditsListResponse(createQuotaCreditsListCmdForTests());

        Assert.assertEquals(expectedQuotaCreditsResponse, result.first().get(0));
        Assert.assertEquals(1, (int) result.second());
    }

    private QuotaCreditsListCmd createQuotaCreditsListCmdForTests() {
        Mockito.doReturn(false).when(accountManagerMock).isNormalUser(Mockito.anyLong());
        QuotaCreditsListCmd cmd = new QuotaCreditsListCmd();
        cmd.setAccountId(1L);
        cmd.setDomainId(2L);
        return cmd;
    }

    @Test(expected = InvalidParameterValueException.class)
    public void getCreditsForQuotaCreditsListTestThrowsInvalidParameterValueExceptionWhenBothAccountIdAndDomainIdAreNull() {
        QuotaCreditsListCmd cmd = new QuotaCreditsListCmd();

        quotaResponseBuilderSpy.getCreditsForQuotaCreditsList(cmd);
    }

    @Test(expected = InvalidParameterValueException.class)
    public void getCreditsForQuotaCreditsListTestThrowsInvalidParameterValueExceptionWhenStartDateIsAfterEndDate() {
        QuotaCreditsListCmd cmd = createQuotaCreditsListCmdForTests();
        cmd.setStartDate(new Date());
        cmd.setEndDate(DateUtils.addDays(new Date(), -1));

        quotaResponseBuilderSpy.getCreditsForQuotaCreditsList(cmd);
    }

    @Test(expected = PermissionDeniedException.class)
    public void getCreditsForQuotaCreditsListTestThrowsPermissionDeniedExceptionWhenDomainIdIsProvidedAndCallerIsNormalUser() {
        QuotaCreditsListCmd cmd = createQuotaCreditsListCmdForTests();
        Mockito.doReturn(true).when(accountManagerMock).isNormalUser(Mockito.anyLong());

        quotaResponseBuilderSpy.getCreditsForQuotaCreditsList(cmd);
    }

    @Test
    public void getCreditsForQuotaCreditsListTestReturnsData() {
        QuotaCreditsListCmd cmd = createQuotaCreditsListCmdForTests();
        List<QuotaCreditsVO> expected = new ArrayList<>();
        expected.add(new QuotaCreditsVO());

        Mockito.doReturn(expected).when(quotaCreditsDaoMock).findCredits(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        List<QuotaCreditsVO> result = quotaResponseBuilderSpy.getCreditsForQuotaCreditsList(cmd);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void getCreditorForQuotaCreditsListTestReturnsUserFromMapWhenMapHasCreditor() {
        Long creditorId = 1L;
        Map<Long, UserVO> userMap = new HashMap<>();

        userMap.put(creditorId, userVoMock);
        Mockito.doReturn(creditorId).when(quotaCreditsVoMock).getUpdatedBy();

        UserVO result = quotaResponseBuilderSpy.getCreditorForQuotaCreditsList(quotaCreditsVoMock, userMap);

        Assert.assertEquals(userVoMock, result);
    }

    @Test
    public void getCreditorForQuotaCreditsListTestGetsCreditorFromDatabaseAndAddsItToMapWhenMapDoesNotHaveCreditor() {
        Long creditorId = 1L;
        Map<Long, UserVO> userMap = new HashMap<>();

        Mockito.doReturn(creditorId).when(quotaCreditsVoMock).getUpdatedBy();
        Mockito.doReturn(userVoMock).when(userDaoMock).findByIdIncludingRemoved(creditorId);

        UserVO result = quotaResponseBuilderSpy.getCreditorForQuotaCreditsList(quotaCreditsVoMock, userMap);

        Assert.assertEquals(userVoMock, result);
        Assert.assertEquals(userVoMock, userMap.get(creditorId));
    }

    @Test
    public void getCreditorForQuotaCreditsTestReturnsCreditorWhenCreditorExists() {
        Long creditorId = 1L;

        Mockito.when(quotaCreditsVoMock.getUpdatedBy()).thenReturn(creditorId);
        Mockito.doReturn(userVoMock).when(userDaoMock).findByIdIncludingRemoved(creditorId);

        UserVO result = quotaResponseBuilderSpy.getCreditorForQuotaCredits(quotaCreditsVoMock);

        Assert.assertEquals(userVoMock, result);
    }

    @Test(expected = CloudRuntimeException.class)
    public void getCreditorForQuotaCreditsTestThrowsCloudRuntimeExceptionWhenCreditorDoesNotExist() {
        quotaResponseBuilderSpy.getCreditorForQuotaCredits(quotaCreditsVoMock);
    }

    @Test
    public void createQuotaCreditsResponseTestReturnsObject() {
        QuotaCreditsResponse expected = new QuotaCreditsResponse();
        expected.setCreditorUserId("test_uuid");
        expected.setCreditorUsername("test_name");
        expected.setCredit(new BigDecimal(41.5));
        expected.setCreditedOn(new Date());
        expected.setCurrency(QuotaConfig.QuotaCurrencySymbol.value());
        expected.setObjectName("credit");

        Mockito.when(userVoMock.getUuid()).thenReturn(expected.getCreditorUserId());
        Mockito.when(userVoMock.getUsername()).thenReturn(expected.getCreditorUsername());
        Mockito.when(quotaCreditsVoMock.getCredit()).thenReturn(expected.getCredit());
        Mockito.when(quotaCreditsVoMock.getUpdatedOn()).thenReturn(expected.getCreditedOn());

        QuotaCreditsResponse result = quotaResponseBuilderSpy.createQuotaCreditsResponse(quotaCreditsVoMock, userVoMock);

        Assert.assertEquals(expected.getCreditorUserId(), result.getCreditorUserId());
        Assert.assertEquals(expected.getCreditorUsername(), result.getCreditorUsername());
        Assert.assertEquals(expected.getCredit(), result.getCredit());
        Assert.assertEquals(expected.getCreditedOn(), result.getCreditedOn());
        Assert.assertEquals(expected.getCurrency(), result.getCurrency());
        Assert.assertEquals(expected.getObjectName(), result.getObjectName());
    }

    @Test
    public void validateActivationRuleTestValidateActivationRuleReturnValidScriptResponse() {
        Mockito.doReturn("if (account.name == 'test') { true } else { false }").when(quotaValidateActivationRuleCmdMock).getActivationRule();
        Mockito.doReturn(QuotaTypes.getQuotaType(30)).when(quotaValidateActivationRuleCmdMock).getQuotaType();
        Mockito.doReturn(quotaValidateActivationRuleCmdMock.getActivationRule()).when(jsInterpreterHelperMock).replaceScriptVariables(Mockito.anyString(), Mockito.any());

        QuotaValidateActivationRuleResponse response = quotaResponseBuilderSpy.validateActivationRule(quotaValidateActivationRuleCmdMock);

        Assert.assertTrue(response.isValid());
    }

    @Test
    public void validateActivationRuleTestUsageTypeIncompatibleVariableReturnInvalidScriptResponse() {
        Mockito.doReturn("if (value.osName == 'test') { true } else { false }").when(quotaValidateActivationRuleCmdMock).getActivationRule();
        Mockito.doReturn(QuotaTypes.getQuotaType(30)).when(quotaValidateActivationRuleCmdMock).getQuotaType();
        Mockito.doReturn(quotaValidateActivationRuleCmdMock.getActivationRule()).when(jsInterpreterHelperMock).replaceScriptVariables(Mockito.anyString(), Mockito.any());
        Mockito.when(jsInterpreterHelperMock.getScriptVariables(quotaValidateActivationRuleCmdMock.getActivationRule())).thenReturn(Set.of("value.osName"));

        QuotaValidateActivationRuleResponse response = quotaResponseBuilderSpy.validateActivationRule(quotaValidateActivationRuleCmdMock);

        Assert.assertFalse(response.isValid());
    }

    @Test
    public void validateActivationRuleTestActivationRuleWithSyntaxErrorsReturnInvalidScriptResponse() {
        Mockito.doReturn("{ if (account.name == 'test') { true } else { false } }}").when(quotaValidateActivationRuleCmdMock).getActivationRule();
        Mockito.doReturn(QuotaTypes.getQuotaType(1)).when(quotaValidateActivationRuleCmdMock).getQuotaType();
        Mockito.doReturn(quotaValidateActivationRuleCmdMock.getActivationRule()).when(jsInterpreterHelperMock).replaceScriptVariables(Mockito.anyString(), Mockito.any());

        QuotaValidateActivationRuleResponse response = quotaResponseBuilderSpy.validateActivationRule(quotaValidateActivationRuleCmdMock);

        Assert.assertFalse(response.isValid());
    }

    @Test
    public void isScriptVariablesValidTestUnsupportedUsageTypeVariablesReturnFalse() {
        Set<String> scriptVariables = new HashSet<>(List.of("value.computingResources.cpuNumber", "account.name", "zone.id"));
        List<String> usageTypeVariables = List.of("value.virtualSize", "account.name", "zone.id");

        boolean isScriptVariablesValid = quotaResponseBuilderSpy.isScriptVariablesValid(scriptVariables, usageTypeVariables);

        Assert.assertFalse(isScriptVariablesValid);
    }

    @Test
    public void isScriptVariablesValidTestSupportedUsageTypeVariablesReturnTrue() {
        Set<String> scriptVariables = new HashSet<>(List.of("value.computingResources.cpuNumber", "account.name", "zone.id"));
        List<String> usageTypeVariables = List.of("value.computingResources.cpuNumber", "account.name", "zone.id");

        boolean isScriptVariablesValid = quotaResponseBuilderSpy.isScriptVariablesValid(scriptVariables, usageTypeVariables);

        Assert.assertTrue(isScriptVariablesValid);
    }

    @Test
    public void isScriptVariablesValidTestVariablesUnrelatedToUsageTypeReturnTrue() {
        Set<String> scriptVariables = new HashSet<>(List.of("variable1.valid", "variable2.valid.", "variable3.valid"));
        List<String> usageTypeVariables = List.of("project.name", "account.id", "domain.path");

        boolean isScriptVariablesValid = quotaResponseBuilderSpy.isScriptVariablesValid(scriptVariables, usageTypeVariables);

        Assert.assertTrue(isScriptVariablesValid);
    }

    @Test
    public void injectUsageTypeVariablesTestReturnInjectedVariables() {
        JsInterpreter interpreter = Mockito.mock(JsInterpreter.class);

        Map<String, String> formattedVariables = quotaResponseBuilderSpy.injectUsageTypeVariables(interpreter, List.of("account.name", "zone.name"));

        Assert.assertTrue(formattedVariables.containsValue("accountname"));
        Assert.assertTrue(formattedVariables.containsValue("zonename"));
    }
}
