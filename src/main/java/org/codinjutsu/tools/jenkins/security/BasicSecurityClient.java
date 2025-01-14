/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.security;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.util.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


class BasicSecurityClient extends DefaultSecurityClient {

    private final String username;
    private String password = null;


    BasicSecurityClient(String username, String password, String crumbData, int connectionTimout) {
        super(crumbData, username, password, connectionTimout);
        this.username = username;
        this.password = password;
    }

    @Nullable
    @Override
    public String connect(URL url) {
        return doAuthentication(url);
    }

    @Nullable
    private String doAuthentication(URL jenkinsUrl) throws AuthenticationException {
        final HttpPost p = new HttpPost(jenkinsUrl.toString());
        p.addHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");

        if (isCrumbDataSet()) {
            p.addHeader(jenkinsVersion.getCrumbName(), crumbData);
        }

        try {
            HttpResponse response = this.httpClient.execute(p);
            final String responseBody;
            try(InputStream inputStream = response.getEntity().getContent()) {
                responseBody = IOUtils.toString(inputStream);
            }
            if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
               checkResponse(response.getStatusLine().getStatusCode(), responseBody);
            }
            return responseBody;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            p.releaseConnection();
        }
        //
//        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
//            httpClient.getState().setCredentials(
//                    new AuthScope(jenkinsUrl.getHost(), jenkinsUrl.getPort()),
//                    new UsernamePasswordCredentials(username, password));
//        }
//
//        httpClient.getParams().setAuthenticationPreemptive(true);
//
//        PostMethod post = new PostMethod(jenkinsUrl.toString());
//        post.addRequestHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
//
//        try {
//            if (isCrumbDataSet()) {
//                post.addRequestHeader(jenkinsVersion.getCrumbName(), crumbData);
//            }
//
//            post.setDoAuthentication(true);
//            int responseCode = httpClient.executeMethod(post);
//            final String responseBody;
//            try(InputStream inputStream = post.getResponseBodyAsStream()) {
//                responseBody = IOUtils.toString(inputStream, post.getResponseCharSet());
//            }
//            if (responseCode != HttpURLConnection.HTTP_OK) {
//                checkResponse(responseCode, responseBody);
//            }
//            return responseBody;
//        } catch (IOException ioEx) {
//            throw new ConfigurationException(String.format("Error during authentication: %s", ioEx.getMessage()), ioEx);
//        } finally {
//            post.releaseConnection();
//        }
    }
}
