/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.testsupport.conda;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.orient.DatabaseInstance;
import org.sonatype.nexus.orient.DatabaseInstanceNames;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.testsuite.testsupport.RepositoryITSupport;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.experimental.categories.Category;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

/**
 * Support for Conda ITs.
 *
 * @since 3.19
 */
@Category(CondaTestGroup.class)
public class CondaITSupport
    extends RepositoryITSupport
{
  public static final String PROXY_REPO_NAME = "conda-proxy";

  public static final String ROOT_LINUX = "linux-64";

  public static final String REPODATA = "repodata.json";

  public static final String REPODATA_MAIN = "main/";

  public static final String REPODATA_LINUX_64 = REPODATA_MAIN + ROOT_LINUX + "/" + REPODATA;

  protected static final String CONTENT_TYPE = "application/x-bzip2";

  public static final String PACKAGE_755_FULLNAME = "curl-7.55.1-h78862de_4.tar.bz2";

  public static final String PACKAGE_765_FULLNAME = "curl-7.65.3-hbc83047_0.tar.bz2";

  protected static final String PACKAGE_NAME = "curl";

  protected static final String PACKAGE_VERSION = "7.55.1";

  protected static final String PACKAGE_PATH =
      ROOT_LINUX + "/" + PACKAGE_NAME + "/" + PACKAGE_VERSION + "/" + PACKAGE_755_FULLNAME;

  public static final String PATH_TO_THE_PACKAGE =
      ROOT_LINUX + "/" + PACKAGE_NAME + "/";

  @Inject
  @Named(DatabaseInstanceNames.COMPONENT)
  Provider<DatabaseInstance> databaseInstanceProvider;

  public CondaITSupport() {
    testData.addDirectory(resolveBaseFile("target/it-resources/conda"));
  }

  public Repository createCondaProxyRepository(final String name, final String remoteUrl) {
    return repos.createCondaProxy(name, remoteUrl);
  }

  protected List<Asset> findAssets(final String repositoryName) {
    String sql = "SELECT * FROM asset WHERE bucket.repository_name = ?";
    try (ODatabaseDocumentTx tx = databaseInstanceProvider.get().acquire()) {
      tx.begin();
      List<ODocument> results = tx.command(new OCommandSQL(sql)).execute(repositoryName);
      return results.stream().map(this::toAsset).collect(toList());
    }
  }

  private Asset toAsset(final ODocument doc) {
    Asset asset = new Asset();
    asset.name(doc.field("name", String.class).toString());
    asset.contentType(doc.field("content_type", String.class).toString());
    return asset;
  }

  protected static List<Component> findComponents(final Repository repo) {
    try (StorageTx tx = getStorageTx(repo)) {
      tx.begin();
      return newArrayList(tx.browseComponents(tx.findBucket(repo)));
    }
  }
}
