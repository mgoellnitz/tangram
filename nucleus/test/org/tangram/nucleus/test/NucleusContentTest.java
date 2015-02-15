package org.tangram.nucleus.test;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.tangram.mutable.MutableBeanFactory;
import org.tangram.mutable.test.BaseContentTest;
import org.tangram.mutable.test.content.BaseInterface;
import org.tangram.mutable.test.content.SubInterface;
import org.tangram.nucleus.NucleusContent;
import org.tangram.nucleus.test.content.BaseClass;
import org.tangram.nucleus.test.content.SubClass;


/**
 * Test various content related methods.
 *
 * We need a test order to have enhanced classes first, then create some content, and the test this content in
 * a separate test "session".
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NucleusContentTest extends BaseContentTest {

    @Override
    protected BaseInterface createBaseBean(MutableBeanFactory beanFactory) throws Exception {
        return beanFactory.createBean(BaseClass.class);
    }


    @Override
    protected SubInterface createSubBean(MutableBeanFactory beanFactory) throws Exception {
        return beanFactory.createBean(SubClass.class);
    }


    @Override
    protected Class<? extends BaseInterface> getBaseClass() {
        return BaseClass.class;
    }


    @Override
    protected void setPeers(BaseInterface base, SubInterface peer) {
        List<BaseClass> peers = new ArrayList<>();
        peers.add((BaseClass) peer);
        ((BaseClass) base).setPeers(peers);
    } // setPeers()


    @Override
    protected int getNumberOfAllClasses() {
        return 14;
    }


    @Override
    protected int getNumberOfClasses() {
        return 3;
    }


    @Test
    public void test0IsEnhanced() {
        Assert.assertTrue("Classes not enhanced", checkMethodPrefixOccurs(NucleusContent.class.getMethods(), "jdo"));
    } // test1IsEnhanced()


    @Test
    public void testStringConversion() throws Exception {
        SubClass subBean = new SubClass();
        String testString = "justateststringtouseforconversiontesting";
        Assert.assertEquals("Nucleus specific conversion methods failed", testString, subBean.checkConversion(testString));
        Assert.assertNull("Nucleus specific conversion methods failed", subBean.checkConversion(null));
    } // testStringConversion()

} // NucleusContentTest
