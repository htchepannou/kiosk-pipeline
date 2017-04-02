package io.tchepannou.kiosk.pipeline.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.tchepannou.kiosk.pipeline.service.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(basePath = "/v1/pipeline", value = "Pipeline API")
@RequestMapping(value = "/v1/pipeline", produces = MediaType.APPLICATION_JSON_VALUE)
public class PipelineController {
    @Autowired
    PipelineService pipelineService;

    @ApiOperation("Return a list of articles")
    @RequestMapping(path = "/reprocess/{feedId}", method = RequestMethod.GET)
    public void reprocess(@PathVariable long feedId){
        pipelineService.reprocess(feedId);
    }
}
