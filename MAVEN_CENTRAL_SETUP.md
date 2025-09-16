# Maven Central Publishing Setup

This document explains how to set up automated publishing to Maven Central using GitHub Actions.

## Prerequisites

1. **Sonatype OSSRH Account**: Create an account at [https://issues.sonatype.org](https://issues.sonatype.org)
2. **Create a JIRA ticket** to request access to publish under `com.arquivolivre` groupId
3. **GPG Key**: Generate a GPG key for signing artifacts

## Required GitHub Secrets

You need to set up the following secrets in your GitHub repository settings:

### 1. OSSRH_USERNAME
Your Sonatype OSSRH username (JIRA username)

### 2. OSSRH_TOKEN
Your Sonatype OSSRH token (not password). Generate this from:
- Go to [https://s01.oss.sonatype.org](https://s01.oss.sonatype.org)
- Login with your JIRA credentials
- Go to your profile (top right) → User Token
- Generate a new token

### 3. GPG_PRIVATE_KEY
Your GPG private key in ASCII-armored format.

To generate and export:
```bash
# Generate a new GPG key (if you don't have one)
gpg --gen-key

# List your keys to find the key ID
gpg --list-secret-keys --keyid-format LONG

# Export the private key (replace KEY_ID with your actual key ID)
gpg --armor --export-secret-keys KEY_ID
```

Copy the entire output (including `-----BEGIN PGP PRIVATE KEY BLOCK-----` and `-----END PGP PRIVATE KEY BLOCK-----`)

### 4. GPG_PASSPHRASE
The passphrase you used when creating your GPG key.

## Setting up GitHub Secrets

1. Go to your GitHub repository
2. Click on **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each of the four secrets listed above

## Publishing Process

### Automatic SNAPSHOT Publishing
- Every push to `main`/`master` branch will automatically publish a SNAPSHOT version to Maven Central
- The version in `pom.xml` should end with `-SNAPSHOT`

### Release Publishing
1. Create a git tag with version number: `git tag v0.4.0`
2. Push the tag: `git push origin v0.4.0`
3. GitHub Actions will automatically:
   - Update the version in pom.xml (remove -SNAPSHOT)
   - Build and publish to Maven Central
   - Create a GitHub Release

## Manual Publishing

You can also trigger publishing manually:
1. Go to **Actions** tab in GitHub
2. Select **Maven Central Publish** workflow
3. Click **Run workflow**
4. Choose the branch and run

## Verification

After publishing, you can verify your artifacts at:
- **Snapshots**: [https://s01.oss.sonatype.org/content/repositories/snapshots/com/arquivolivre/mongocom/](https://s01.oss.sonatype.org/content/repositories/snapshots/com/arquivolivre/mongocom/)
- **Releases**: [https://search.maven.org/artifact/com.arquivolivre/mongocom](https://search.maven.org/artifact/com.arquivolivre/mongocom)

## Troubleshooting

### GPG Issues
If you get GPG errors, make sure:
- Your GPG key is properly formatted (with headers/footers)
- The passphrase is correct
- The key hasn't expired

### Authentication Issues
- Verify your OSSRH credentials are correct
- Make sure your token (not password) is used
- Check that you have permission to publish under `com.arquivolivre`

### Build Issues
- Check that all tests pass
- Verify Java 17 compatibility
- Ensure all required Maven plugins are configured correctly

## Example Usage After Publishing

Once published, users can add your library to their projects:

```xml
<dependency>
    <groupId>com.arquivolivre</groupId>
    <artifactId>mongocom</artifactId>
    <version>0.4-SNAPSHOT</version>
</dependency>
```

For releases:
```xml
<dependency>
    <groupId>com.arquivolivre</groupId>
    <artifactId>mongocom</artifactId>
    <version>0.4.0</version>
</dependency>
```

