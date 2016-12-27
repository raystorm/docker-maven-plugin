package io.fabric8.maven.docker.service;


import java.util.HashMap;
import java.util.Map;

import io.fabric8.maven.docker.access.DockerAccess;
import io.fabric8.maven.docker.access.VolumeCreateConfig;
import io.fabric8.maven.docker.config.VolumeConfiguration;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONParser;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *  Basic Unit Tests for {@link VolumeService}
 *
 *  @author Tom Burton
 *  @version Dec 16, 2016
 */
public class VolumeServiceTest {
   private VolumeCreateConfig volumeConfig;

   @Mocked
   private DockerAccess docker;

   private VolumeService volumeService;

   /**
    * @throws java.lang.Exception
    */
   @Before
   public void setUp() throws Exception {
      volumeService = new VolumeService(docker);
   }

   /*
    * methods to test
    *
    * volumeService.createVolumeConfig(volumeName, driver, driverOpts, labels);
    * volumeService.removeVolume(volumeName);
    */

   private Map<String, String > withMap(String what) {
      Map<String, String> map = new HashMap<String, String>();
      map.put(what + "Key1", "value1");
      map.put(what + "Key2", "value2");

      return map;
   }

   @Test
   public void testCreateVolumeConfig() throws Exception {
      final VolumeConfiguration config =
          new VolumeConfiguration.Builder()
              .name("testVolume")
              .driver("test")
              .opts(withMap("opts"))
              .labels(withMap("labels"))
              .build();

      new Expectations() {{
         // Use a 'delegate' to verify the argument given directly. No need
         // for an 'intermediate' return method in the service just to check this.
         docker.createVolume(with(new Delegate<VolumeCreateConfig>() {
            void check(VolumeCreateConfig vcc) {
               assertThat(vcc.getName(), is("testVolume"));
               JSONObject vccJson = (JSONObject) JSONParser.parseJSON(vcc.toJson());
               assertEquals(vccJson.get("Driver"), "test");
            }
         })); result = "testVolume";
      }};

      String volume = new VolumeService(docker).createVolume(config);
      assertThat(volume, is("testVolume"));
   }

   @Test
   public void testCreateVolume() throws Exception {
      VolumeConfiguration vc = new VolumeConfiguration.Builder()
                                     .name("testVolume")
                                     .driver("test").opts(withMap("opts"))
                                     .labels(withMap("labels"))
                                     .build();

      new Expectations() {{
         docker.createVolume((VolumeCreateConfig)any); result = "testVolume";
      }};

      assertThat(vc.getName(), is("testVolume"));
      String name = volumeService.createVolume(vc);

      assertThat(name, is("testVolume"));
   }

   @Test
   public void testRemoveVolume() throws Exception {
      VolumeConfiguration vc = new VolumeConfiguration.Builder()
            .name("testVolume")
            .driver("test").opts(withMap("opts"))
            .labels(withMap("labels"))
            .build();

      new Expectations() {{
         docker.createVolume((VolumeCreateConfig) any); result = "testVolume";
         docker.removeVolume("testVolume");
      }};
      String name = volumeService.createVolume(vc);
      
      volumeService.removeVolume(name);

      //TODO: add verification here
   }

}
