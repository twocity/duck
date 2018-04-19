/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package duck

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.security.KeyStore
import java.security.KeyStore.PasswordProtection
import java.security.KeyStore.PrivateKeyEntry
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Signing information.
 *
 * Both the {@link PrivateKey} and the {@link X509Certificate} are guaranteed to be non-null.
 *
 * kotlin version of AOSP source code:
 * https://android.googlesource.com/platform/tools/base/+/master/sdk-common/src/main/java/com/android/ide/common/signing/
 *
 */

class CertificateInfo private constructor(val key: PrivateKey, val certificate: X509Certificate) {

  companion object {
    /**
     * Returns the CertificateInfo for the given signing configuration.
     *
     * @param storeType an optional type of keystore; if {@code null} the default
     * @param storeFile the file where the store should be created
     * @param storePassword a password for the key store
     * @param keyPassword a password for the key
     * @param keyAlias the alias under which the key is stored in the store
     * @return the certificate info if it could be loaded
     * @throws KeytoolException If the password is wrong
     * @throws FileNotFoundException If the store file cannot be found
     */
    fun read(storeType: String?,
        storeFile: File,
        storePassword: String,
        keyPassword: String,
        keyAlias: String): CertificateInfo {
      try {
        val keyStore = KeyStore.getInstance(storeType ?: KeyStore.getDefaultType())
        FileInputStream(storeFile).use {
          keyStore.load(it, storePassword.toCharArray())
        }
        val entry = keyStore.getEntry(keyAlias, PasswordProtection(keyPassword.toCharArray()))
            as PrivateKeyEntry
        return CertificateInfo(entry.privateKey, entry.certificate as X509Certificate)
      } catch (e: Exception) {
        throw KeytoolException("Failed to read key $keyAlias from store $storeFile: ${e.message}")
      }
    }
  }
}

class KeytoolException(msg: String) : Exception(msg)