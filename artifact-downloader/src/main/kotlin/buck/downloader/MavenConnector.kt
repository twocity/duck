// Copyright 2015 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package buck.downloader

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.AbstractRepositoryListener
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory

/**
 * Modify from bazel source code
 * Connections to Maven repositories.
 */
class MavenConnector(private val localRepositoryPath: String) {

  fun newRepositorySystemSession(system: RepositorySystem): RepositorySystemSession {
    val session = MavenRepositorySystemUtils.newSession()
    val localRepo = LocalRepository(localRepositoryPath)
    session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)
    session.transferListener = object : AbstractTransferListener() {

    }
    session.repositoryListener = object : AbstractRepositoryListener() {

    }
    return session
  }

  fun newRepositorySystem(): RepositorySystem {
    val locator = MavenRepositorySystemUtils.newServiceLocator()
    locator.addService(RepositoryConnectorFactory::class.java,
        BasicRepositoryConnectorFactory::class.java)
    locator.addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
    locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
    return locator.getService(RepositorySystem::class.java)
  }
}

