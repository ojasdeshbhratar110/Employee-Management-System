import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { runInNewContext } from "node:vm";
import test from "node:test";

const source = readFileSync(new URL("../script.js", import.meta.url), "utf8");
const helper = source.match(/function escapeHtml\(value\) \{[\s\S]*?\n\}/);
assert.ok(helper, "script.js must define the shared HTML escaping helper");

const sandbox = {};
runInNewContext(`${helper[0]}; globalThis.escapeHtml = escapeHtml;`, sandbox);

test("escapes HTML metacharacters used in employee text fields", () => {
    assert.equal(
        sandbox.escapeHtml(`<img src=x onerror='alert("x")'>&`),
        "&lt;img src=x onerror=&#39;alert(&quot;x&quot;)&#39;&gt;&amp;"
    );
});

test("converts nullish values to safe empty text", () => {
    assert.equal(sandbox.escapeHtml(null), "");
    assert.equal(sandbox.escapeHtml(undefined), "");
});