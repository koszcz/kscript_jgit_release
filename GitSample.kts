@file:DependsOn("org.eclipse.jgit:org.eclipse.jgit:5.1.1.201809181055-r")

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand.*
import org.eclipse.jgit.transport.RefSpec
import java.io.File

//echo "USAGE: kscript GitSample.kts 1.1.1"

val versionName = args[0]

val git = Git.open(File(""))

git.stashCreate().call()

git.checkout().apply {
    setCreateBranch(true)
    setName("release/$versionName")
}.call()

git.commit().apply {
    setAllowEmpty(true)
    setAll(true)
    setMessage("Update to $versionName")
}.call()

git.checkout().apply {
    setName("master")
}.call()

git.pull().apply {
    setRebase(true)
}.call()

val releaseRef = git.branchList().call().find {
    it.name == "refs/heads/release/$versionName"
}

git.merge().apply {
    setFastForward(FastForwardMode.NO_FF)
    include(releaseRef)
}.call()

git.push().apply {
    setRemote("origin")
    setRefSpecs(listOf(RefSpec("master:master")))
}.call()

git.tag().apply {
    setName(versionName)
}.call()

git.push().apply {
    setPushTags()
}.call()

git.checkout().apply {
    setName("develop")
}.call()

git.merge().apply {
    setFastForward(FastForwardMode.NO_FF)
    include(releaseRef)
}.call()

git.push().apply {
    setRemote("origin")
    setRefSpecs(listOf(RefSpec("develop:develop")))
}.call()

git.branchDelete().apply {
    setBranchNames("release/$versionName")
}.call()
