package io.tchepannou.kiosk.pipeline.persistence.repository;

import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends CrudRepository<Asset, Long> {
    Asset findByLinkAndTargetAndType(Link link, Link target, AssetTypeEnum type);

    List<Asset> findByTargetAndType(Link target, AssetTypeEnum type);
}
