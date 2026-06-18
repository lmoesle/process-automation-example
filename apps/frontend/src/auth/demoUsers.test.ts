import { describe, expect, it } from "vitest";
import { demoUsers, getBasicAuthHeader } from "./demoUsers";

describe("demoUsers", () => {
  it("creates a Basic Auth header for a selected demo user", () => {
    expect(getBasicAuthHeader(demoUsers[0])).toBe(`Basic ${btoa("john:test")}`);
  });
});
