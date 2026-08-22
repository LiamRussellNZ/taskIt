import { spawnSync } from 'node:child_process';

const defaults = {
  baseUrl: 'http://localhost:8080',
  askers: 4,
  doers: 6,
  tasks: 20
};

const askerNames = ['Leia Nova', 'Tony Orbit', 'Diana Signal', 'Miles Vector', 'Dana Scully', 'Jean Luc'];
const doerNames = ['Clark Forge', 'Natasha Byte', 'Kara Zenith', 'Peter Quill', 'Sam Beacon', 'Rory Vega'];
const taskTemplates = [
  { title: 'Help set up a sci-fi movie marathon', description: 'Need help arranging seats, snacks, and a projector for a weekend science-fiction marathon.', category: 'Events', location: 'Wellington', remote: false },
  { title: 'Organize a comic collection', description: 'Sort a mixed Marvel and DC comic collection into readable runs and storage boxes.', category: 'Organizing', location: 'Auckland', remote: false },
  { title: 'Build a superhero trivia quiz', description: 'Create a friendly trivia quiz covering comic-book heroes, teams, and story arcs.', category: 'Writing', location: 'Remote', remote: true },
  { title: 'Recommend a new TV series', description: 'Suggest a character-driven science-fiction, mystery, or superhero TV series for a watch list.', category: 'Research', location: 'Remote', remote: true },
  { title: 'Help decorate a fandom watch-party space', description: 'Help arrange themed decorations, seating, and lighting for a television watch party.', category: 'Events', location: 'Christchurch', remote: false },
  { title: 'Create a comic-inspired playlist', description: 'Put together an upbeat playlist for reading comics and watching action shows.', category: 'Creative', location: 'Remote', remote: true },
  { title: 'Catalog collectible figures', description: 'Photograph and catalogue a small collection of sci-fi and superhero figures in a spreadsheet.', category: 'Organizing', location: 'Hamilton', remote: false },
  { title: 'Design a TV watch-list tracker', description: 'Create a simple spreadsheet tracker for seasons, episodes, and recommendations.', category: 'Technology', location: 'Remote', remote: true }
];

function parseOptions(argumentsList) {
  const options = { ...defaults };

  for (let index = 0; index < argumentsList.length; index += 2) {
    const option = argumentsList[index];
    const value = argumentsList[index + 1];

    if (value === undefined) {
      throw new Error(`Missing value for ${option}.`);
    }

    if (option === '--base-url') {
      options.baseUrl = value.replace(/\/$/, '');
    } else if (option === '--askers' || option === '--doers' || option === '--tasks') {
      const count = Number(value);
      const key = option.slice(2);
      if (!Number.isInteger(count) || count < 1 || count > (key === 'tasks' ? 200 : 50)) {
        throw new Error(`${option} must be a whole number between 1 and ${key === 'tasks' ? 200 : 50}.`);
      }
      options[key] = count;
    } else {
      throw new Error(`Unknown option: ${option}.`);
    }
  }

  return options;
}

function post(baseUrl, path, body, headers = {}) {
  const argumentsList = ['--silent', '--show-error', '--fail', '--request', 'POST', `${baseUrl}${path}`,
    '--header', 'Content-Type: application/json'];

  for (const [name, value] of Object.entries(headers)) {
    argumentsList.push('--header', `${name}: ${value}`);
  }

  argumentsList.push('--data', JSON.stringify(body));
  const result = spawnSync('curl', argumentsList, { encoding: 'utf8' });

  if (result.error) {
    throw new Error(`Unable to run curl: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(result.stderr.trim() || `curl exited with status ${result.status}.`);
  }

  return JSON.parse(result.stdout);
}

function createUser(baseUrl, displayName, role, index, runId) {
  return post(baseUrl, '/api/users', {
    displayName,
    email: `${role.toLowerCase()}.${runId}.${index}@seed.taskit.test`,
    roles: [role]
  });
}

function main() {
  const options = parseOptions(process.argv.slice(2));
  const runId = Date.now();
  const askers = Array.from({ length: options.askers }, (_, index) =>
    createUser(options.baseUrl, `${askerNames[index % askerNames.length]} ${index + 1}`, 'ASKER', index, runId));

  Array.from({ length: options.doers }, (_, index) =>
    createUser(options.baseUrl, `${doerNames[index % doerNames.length]} ${index + 1}`, 'DOER', index, runId));

  for (let index = 0; index < options.tasks; index += 1) {
    const template = taskTemplates[index % taskTemplates.length];
    const asker = askers[index % askers.length];
    post(options.baseUrl, '/api/tasks', {
      title: `${template.title} #${index + 1}`,
      description: template.description,
      category: template.category,
      location: template.location,
      remote: template.remote
    }, { 'X-User-Id': asker.id });
  }

  console.log(`Created ${options.askers} ASKERS, ${options.doers} DOERS, and ${options.tasks} open tasks at ${options.baseUrl}.`);
}

try {
  main();
} catch (error) {
  console.error(`Seeding failed. Confirm TaskIt is running and curl is installed. ${error.message}`);
  process.exitCode = 1;
}
