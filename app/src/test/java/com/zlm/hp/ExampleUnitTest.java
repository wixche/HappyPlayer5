package com.zlm.hp;

import com.zlm.hp.util.RandomUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testRandom(){
        int size = 5;
        RandomUtil.setNums(size);
        for(int i = 0; i < size * 2; i++){
            int num = RandomUtil.createRandomNum();
            System.out.println("random result ->" + num + "");
        }
    }
}