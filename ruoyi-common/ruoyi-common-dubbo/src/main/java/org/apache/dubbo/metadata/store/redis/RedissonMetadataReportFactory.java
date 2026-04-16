package org.apache.dubbo.metadata.store.redis;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.metadata.report.MetadataReport;
import org.apache.dubbo.metadata.report.support.AbstractMetadataReportFactory;

/**
 * RedisMetadataReportFactory.
 */
public class RedissonMetadataReportFactory extends AbstractMetadataReportFactory {

    @Override
    public MetadataReport createMetadataReport(URL url) {
        return new RedissonMetadataReport(url);
    }

}
